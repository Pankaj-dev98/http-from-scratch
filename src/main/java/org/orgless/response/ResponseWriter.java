package org.orgless.response;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class ResponseWriter {

    public enum State { INITIALIZED, STATUS_WRITTEN, HEADERS_WRITTEN, DONE }

    private OutputStream out;

    private State state = State.INITIALIZED;

    public ResponseWriter(OutputStream out) {
        this.out = out;
    }

    public void writeStatusLine(HttpStatus status) throws IOException  {
        if (state != State.INITIALIZED)
            throw new IllegalStateException("Error: Status line already written or headers already sent.");

        String statusLine = "HTTP/1.1 " + status.getStatusCode() + " " +
            status.name().toLowerCase().replace("_", " ") + "\r\n";

        out.write(statusLine.getBytes(StandardCharsets.UTF_8));
        System.out.print("Attached status line:" + statusLine);
        state = State.STATUS_WRITTEN;
    }

    public void writeHeaders(Map<String, String> headers) throws IOException {
        if (state != State.STATUS_WRITTEN)
            throw new IllegalStateException("Error: Must write status line before writing headers.");

        String date = DateTimeFormatter.RFC_1123_DATE_TIME
            .withZone(ZoneOffset.UTC)
            .format(Instant.ofEpochMilli(System.currentTimeMillis()));
        headers.put("Date", date);

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
        state = State.STATUS_WRITTEN;
    }

    public void writeBody(byte[] body) throws IOException {
        if (state != State.STATUS_WRITTEN)
            throw new IllegalStateException("Error: Must write headers before writing body");

        if (body != null && body.length > 0)
            out.write(body);

        out.flush();
        state = State.DONE;
    }

    public static Map<String, String> getDefaultHeaders(int contentLen, String contentType) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Length", contentLen + "");
        headers.put("Connection", "close");
        headers.put("Content-Type", contentType);

        return headers;
    }
}