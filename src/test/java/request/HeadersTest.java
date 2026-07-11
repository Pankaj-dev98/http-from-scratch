package request;

import org.junit.jupiter.api.Test;
import org.orgless.parsers.RequestParser;
import org.orgless.request_response.ChunkReader;
import org.orgless.request_response.Headers;
import org.orgless.request_response.HttpRequest;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class HeadersTest {

    @Test
    public void testValidSingleHeader() {
        Headers headers = new Headers();
        byte[] data = "Host: localhost:42069\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        Headers.ParseResult result = headers.parse(data);

        assertThat(headers.get("Host")).isEqualTo("localhost:42069");
        assertThat(result.bytesConsumed()).isEqualTo(23);
        assertThat(result.isDone()).isFalse();
    }

    @Test
    public void testInvalidSpacingHeader() {
        Headers headers = new Headers();
        byte[] data = "       Host: localhost:42069\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> {
            headers.parse(data);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testValidSingleHeaderCaseInsensitive() {
        Headers headers = new Headers();
        byte[] data = "CoNtEnT-LeNgTh: 42\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        Headers.ParseResult result = headers.parse(data);

        assertThat(headers.get("content-length")).isEqualTo("42");
        assertThat(result.bytesConsumed()).isEqualTo(20);
        assertThat(result.isDone()).isFalse();
    }

    @Test
    public void testInvalidCharacterInHeaderKey() {
        Headers headers = new Headers();
        // '©' is not a valid tchar
        byte[] data = "H©st: localhost:42069\r\n\r\n".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> {
            headers.parse(data);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testRepeatingKeys() {
        Headers headers = new Headers();

        byte[] data1 = "content-length: 420\r\nAccept: text/html\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        headers.parse(data1);

        byte[] data2 = "Content-Length: 69\r\n\r\n".getBytes(StandardCharsets.UTF_8);
        headers.parse(data2);

        System.out.println(headers);
        assertThat(headers.get("content-length"))
            .isEqualTo("420, 69");
    }

    @Test
    public void testStandardHeaders() throws Exception {
        String rawData = "GET / HTTP/1.1\r\nHost: localhost:42069\r\nUser-Agent: curl/7.81.0\r\nAccept: */*\r\n\r\n";

        InputStream in = new ChunkReader(rawData.getBytes(StandardCharsets.UTF_8), 3, 0);
        HttpRequest req = RequestParser.parseStream(in);

        System.out.println(req.getHeaders());
        assertThat(req).isNotNull();
        assertThat(req.getHeaders().get("host")).isEqualTo("localhost:42069");
        assertThat(req.getHeaders().get("User-Agent")).isEqualTo("curl/7.81.0");
        assertThat(req.getHeaders().get("accept")).isEqualTo("*/*");
    }

    @Test
    public void testMalformedHeader() {
        // missing colon after "Host"
        String rawData = "GET / HTTP/1.1\r\nHost localhost:42069\r\n\r\n";
        ChunkReader reader = new ChunkReader(rawData.getBytes(StandardCharsets.UTF_8), 3, 0);

        assertThatThrownBy(() -> {
            RequestParser.parseStream(reader);
        }).isInstanceOf(IllegalArgumentException.class);
    }
}