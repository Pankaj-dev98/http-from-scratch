package org.orgless.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final static String HTTP_1_1 = "HTTP/1.1";
    private final HttpStatus httpStatus;

    private final Map<String, String> headers = new HashMap<>();
    private byte[] body;

    public HttpResponse(HttpStatus httpStatus, byte[] body) {
        this.httpStatus = httpStatus;
        this.body = body;
        getDefaultHeaders(body != null ? body.length : 0);
    }

    private void getDefaultHeaders(int contentLen) {
        headers.put("Content-Length", contentLen + "");
        headers.put("Connection", "close");
        headers.put("Content-Type", "text/plain");
    }

    public void addHeader(String key, String value) {
        if (headers.containsKey(key)) {
            headers.put(key, headers.get(key) + ", " + value);
            return;
        }
        headers.put(key, value);
    }

    public void setHeader(String key, String value) {
        headers.put(key, value);
    }

    public byte[] toBytes() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        writeStatusLine(out);
        writeHeaders(out);

        if (body != null && body.length > 0)
            out.write(body);

        return out.toByteArray();
    }

    private void writeStatusLine(OutputStream out) throws IOException {
        int statusCode = httpStatus.getStatusCode();
        String reasonPhrase = httpStatus.name();
        String statusLine = HTTP_1_1 + " " + statusCode + " " + reasonPhrase + "\r\n";
        out.write(statusLine.getBytes(StandardCharsets.UTF_8));
    }

    // writes headers as well as the leading \r\n chars to separate the payload.
    private void writeHeaders(OutputStream out) throws IOException {
        StringBuilder fieldLinesBuilder = new StringBuilder();
        for (Map.Entry<String, String> e : headers.entrySet()) {
            fieldLinesBuilder
                .append(e.getKey())
                .append(": ")
                .append(e.getValue())
                .append("\r\n");
        }
        fieldLinesBuilder.append("\r\n");
        out.write(fieldLinesBuilder.toString().getBytes(StandardCharsets.UTF_8));
    }
}