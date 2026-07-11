package request;

import org.junit.jupiter.api.Test;
import org.orgless.parsers.RequestParser;
import org.orgless.request_response.ChunkReader;
import org.orgless.request_response.HttpRequest;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class RequestTest {
    @Test
    public void testStandardBody() throws Exception {
        String rawData = "POST /submit HTTP/1.1\r\n" +
            "Host: localhost:42069\r\n" +
            "Content-Length: 13\r\n" +
            "\r\n" +
            "hello world!\n";

        ChunkReader reader = new ChunkReader(rawData.getBytes(StandardCharsets.UTF_8), 3, 0);
        HttpRequest r = RequestParser.parseStream(reader);

        assertThat(r).isNotNull();

        // Convert the raw byte array back to a string for testing
        String bodyStr = new String(r.getBody(), StandardCharsets.UTF_8);
        assertThat(bodyStr).isEqualTo("hello world!\n");
        reader.close();
    }

    @Test
    public void testBodyShorterThanReportedLength() throws Exception {
        String rawData = "POST /submit HTTP/1.1\r\n" +
            "Host: localhost:42069\r\n" +
            "Content-Length: 20\r\n" +
            "\r\n" +
            "partial content";

        ChunkReader reader = new ChunkReader(rawData.getBytes(StandardCharsets.UTF_8), 3, 0);

        // This will throw the "Unexpected EOF" exception we just added
        // to RequestParser because the stream ends before 20 bytes are read!
        assertThatThrownBy(() -> {
            RequestParser.parseStream(reader);
        }).isInstanceOf(IllegalStateException.class);
        reader.close();
    }
}