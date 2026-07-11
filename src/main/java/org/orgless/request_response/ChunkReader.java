package org.orgless.request_response;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.io.InputStream;

@AllArgsConstructor
public class ChunkReader extends InputStream {
    private final byte[] data;
    private final int numBytesPerRead;
    private int pos = 0;

    @Override
    public int read() throws IOException {
        return 0;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (pos >= data.length) return -1;

        int bytesRead = Math.min(len, numBytesPerRead);
        int endIdx    = Math.min(pos + bytesRead, data.length);
        int n = endIdx - pos;

        System.arraycopy(data, pos, b, off, n);
        pos += n;
        return n;
    }
}