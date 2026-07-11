package request;

import org.junit.jupiter.api.Test;
import org.orgless.parsers.RequestParser;
import org.orgless.request_response.ChunkReader;
import org.orgless.request_response.HttpMethod;
import org.orgless.request_response.HttpRequest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

public class HttpRequestTest {

    @Test
    public void testGoodGetRequestLineChunked3() throws Exception {
        String rawData = "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nAccept: */*\r\n\r\n";

        // Pass data and numBytesPerRead (3)
        ChunkReader reader = new ChunkReader(rawData.getBytes(StandardCharsets.UTF_8), 3, 0);
        HttpRequest r = RequestParser.parseStream(reader);

        assertThat(r).isNotNull();
        assertThat(r.getRequestLine().method()).isEqualTo(HttpMethod.GET);
        assertThat(r.getRequestLine().requestTarget()).isEqualTo("/");
        assertThat(r.getRequestLine().httpVersion()).isEqualTo("1.1");
    }

    @Test
    public void testGoodGetRequestLineWithPathChunked1() throws Exception {
        String rawData = "GET /coffee HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nAccept: */*\r\n\r\n";

        // Testing extreme chunking (1 byte at a time)
        ChunkReader reader = new ChunkReader(rawData.getBytes(StandardCharsets.UTF_8), 1, 0);
        HttpRequest r = RequestParser.parseStream(reader);

        assertThat(r).isNotNull();
        assertThat(r.getRequestLine().method()).isEqualTo(HttpMethod.GET);
        assertThat(r.getRequestLine().requestTarget()).isEqualTo("/coffee");
        assertThat(r.getRequestLine().httpVersion()).isEqualTo("1.1");
    }
}