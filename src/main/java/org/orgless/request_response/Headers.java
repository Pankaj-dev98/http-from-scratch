package org.orgless.request_response;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Headers {

    private static final String CRLF = "\r\n";
    private final Map<String, String> headersMap;
    private static final String validSpecialCharacters = "!#$%&'*+-.^_|~";
    private static final int MAX_HEADER_COUNT = 100;

    public Headers() {
        this.headersMap = new HashMap<>();
    }

    public record ParseResult(int bytesConsumed, boolean isDone) {}

    public ParseResult parse(byte[] data) throws IllegalArgumentException {
        String rawData = new String(data, StandardCharsets.UTF_8);

        int crlfIndex = rawData.indexOf(CRLF);
        // not enough data
        if (crlfIndex == -1) return new ParseResult(0, false);
        // end of headers
        if (crlfIndex == 0) return new ParseResult(2, true);

        String line = rawData.substring(0, crlfIndex);
        int colonIndex = line.indexOf(":");

        if (colonIndex == -1)
            throw new IllegalArgumentException("Invalid header format: colon missing");

        String key = line.substring(0, colonIndex);
        if (key.trim().length() != key.length())
            throw new IllegalArgumentException("Leading/trailing spaced in header key");

        // faster than regex
        for (char c : key.toCharArray()) {
            if (!Character.isLetterOrDigit(c) && validSpecialCharacters.indexOf(c) == -1)
                throw new IllegalArgumentException("Invalid characters received in key-" + key);
        }

        String value = line.substring(colonIndex + 1).trim();
        key = key.toLowerCase();

        if (headersMap.size() >= MAX_HEADER_COUNT)
            throw new IllegalStateException("Too many headers received. [max headers allowed = 100]");

        if (!headersMap.containsKey(key)) {
            headersMap.put(key.toLowerCase(), value);
        } else {
            String existingValue = headersMap.get(key);
            String newValue = existingValue + ", " + value;
            headersMap.put(key, newValue);
        }
        return new ParseResult(line.length() + 2, false);
    }

    public String get(String key) {
        return this.headersMap.get(key.toLowerCase());
    }

    @Override
    public String toString() {
        if (headersMap.isEmpty())
            return "";
        StringBuilder build = new StringBuilder();
        for (var e : headersMap.entrySet()) {
            String key = e.getKey();
            String val = e.getValue();

            build.append(key)
                .append(": ")
                .append(val)
                .append('\n');
        }
        return build.deleteCharAt(build.length() - 1).toString();
    }
}