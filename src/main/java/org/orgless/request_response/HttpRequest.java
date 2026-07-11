package org.orgless.request_response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@NoArgsConstructor
public class HttpRequest {

    public static final String CRLF = "\r\n";

    public enum State {
        INITIALIZED, PARSING_HEADERS, PARSING_BODY, DONE;
    }

    // Fieldsy
    @Getter
    @Setter
    private State state = State.INITIALIZED;

    @Getter
    private RequestLine requestLine;

    @Getter
    private final Headers headers = new Headers();

    @Getter
    private byte[] body = new byte[0];

    private int cumulativeHeaderBytes = 0;
    private static final int MAX_HEADER_SIZE = 8192;
    // Methods
    public int parse(byte[] data) throws Exception {
        int totalBytesParsed = 0;

        while (state != State.DONE) {
            byte[] dataToParse = Arrays.copyOfRange(data, totalBytesParsed, data.length);
            if (dataToParse.length == 0) break;

            int bytesParsed = parseSingle(dataToParse);
            if (bytesParsed == 0) break;

            if (state == State.PARSING_HEADERS) {
                cumulativeHeaderBytes += bytesParsed;
                if (cumulativeHeaderBytes > MAX_HEADER_SIZE)
                    throw new IllegalStateException("Cumulative header size too large (size allowed <= 8kb)");
            }

            totalBytesParsed += bytesParsed;
        }
        return totalBytesParsed;
    }

    private int parseSingle(byte[] data) throws Exception {
        return switch (state) {
            case State.INITIALIZED -> {
                int consumed = parseRequestLine(data);
                if (consumed > 0)
                    state = State.PARSING_HEADERS;
                yield consumed;
            }
            case PARSING_HEADERS -> {
                Headers.ParseResult result = headers.parse(data);
                if (result.isDone()) {
                    if (headers.get("content-length") == null)
                        state = State.DONE;
                    else
                        state = State.PARSING_BODY;
                }
                yield result.bytesConsumed();
            }
            case PARSING_BODY -> {

                int expectedLength = Integer.parseInt(headers.get("content-length"));
                byte[] newBody = new byte[body.length + data.length];
                System.arraycopy(body, 0, newBody, 0, body.length);
                System.arraycopy(data, 0, newBody, body.length, data.length);

                body = newBody;
                int currentLength = body.length;
                if (currentLength > expectedLength)
                    throw new IllegalArgumentException("Payload length exceeds length specified in 'content-length'");

                if (currentLength == expectedLength) {
                    state = State.DONE;
                }
                yield data.length;
            }
            case DONE -> throw new IllegalStateException("error: trying to parse request in DONE state.");
        };
    }

    private int parseRequestLine(byte[] data) {
        String rawData = new String(data, StandardCharsets.UTF_8);

        int newLineIndex = rawData.indexOf(CRLF);

        if (newLineIndex == -1) return 0;

        String requestLineStr = rawData.substring(0, newLineIndex);
        String[] parts = requestLineStr.split(" ");

        if (parts.length != 3)
            throw new IllegalArgumentException("Illegal number of parts in request-line");

        // if method has lower case letters, this throws
        HttpMethod method = HttpMethod.valueOf(parts[0]);

        String target = parts[1];
        String fullHttpVersion = parts[2];

        if (!fullHttpVersion.equals("HTTP/1.1"))
            throw new IllegalArgumentException("Invalid version in HttpRequest line");

        String httpVersion = fullHttpVersion.replace("HTTP/", "");
        this.requestLine = new RequestLine(httpVersion, target, method);
        return requestLineStr.length() + 2;
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
            "state=" + state +
            ", requestLine=" + requestLine +
            ", headers=" + headers +
            ", body=" + Arrays.toString(body) +
            ", cumulativeHeaderBytes=" + cumulativeHeaderBytes +
            '}';
    }
}