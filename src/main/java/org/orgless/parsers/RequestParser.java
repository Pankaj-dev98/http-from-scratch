package org.orgless.parsers;


import org.orgless.request_response.HttpRequest;

import java.io.InputStream;
import java.util.Arrays;

public class RequestParser {

    private static final int BUFFER_SIZE = 512;
    private static final int MAX_BUFFER_SIZE = 8192;

    public static HttpRequest parseStream(InputStream reader) throws Exception {
        byte[] buff = new byte[BUFFER_SIZE];
        int readToIndex = 0;

        HttpRequest request = new HttpRequest();

        while (request.getState() != HttpRequest.State.DONE) {

            if (readToIndex >= MAX_BUFFER_SIZE) {
                throw new IllegalStateException("Payload Too Large: Malicious or malformed request exceeded max buffer size.");
            }


            if (readToIndex == buff.length) {
                byte[] newBuff = new byte[Math.min(MAX_BUFFER_SIZE, buff.length * 2)];
                System.arraycopy(buff, 0, newBuff, 0, buff.length);
                buff = newBuff;
            }

            // Read from the reader into the buffer starting at readToIndex
            int bytesRead = reader.read(buff, readToIndex, buff.length - readToIndex);

            // If we hit the end of the reader (io.EOF)
            if (bytesRead == -1) {
                if (request.getState() != HttpRequest.State.DONE)
                    throw new IllegalStateException("Unexpected EOF: Connection closed before request finished parsing");
                break;
            }

            readToIndex += bytesRead;

            byte[] dataToParse = Arrays.copyOfRange(buff, 0, readToIndex);
            int bytesParsed = request.parse(dataToParse);

            if (bytesParsed > 0) {
                int remaining = readToIndex - bytesParsed;
                System.arraycopy(buff, bytesParsed, buff, 0, remaining);
                readToIndex = remaining;
            }
        }
        return request;
    }
}