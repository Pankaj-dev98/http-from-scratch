package org.orgless.server;

import org.orgless.request_response.HttpRequest;
import org.orgless.response.HttpStatus;
import org.orgless.response.ResponseWriter;
import org.orgless.services.FunFactService;
import org.orgless.services.HttpBingoService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.orgless.response.ResponseWriter.getDefaultHeaders;

public class Controller {

    public void home(ResponseWriter writer, HttpRequest request) {
        byte[] payload = null;
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("root-page.html")) {
            if (in == null)
                throw new FileNotFoundException("root-page.html does not exist as a resource");
            payload = in.readAllBytes();
            ok(writer, payload, "text/html");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getFunFact(ResponseWriter w, HttpRequest httpRequest) throws IOException {
        String funFact = FunFactService.getFunFact();
        ok(w, funFact.getBytes(StandardCharsets.UTF_8), "text/plain");
    }

    public void httpBingoProxy(ResponseWriter w, HttpRequest httpRequest) {
        String endpoint = httpRequest.getRequestLine().requestTarget();
        String httpBingoStr = "/httpbingo/";
        if (!endpoint.startsWith(httpBingoStr))
            throw new IllegalArgumentException("Wrong handler called for endpoint " + endpoint);

        final String target = endpoint.substring(httpBingoStr.length() - 1);
        HttpResponse<InputStream> httpBinResponse = HttpBingoService.getResponse(target);

        try {
            if (httpBinResponse.statusCode() > 200) {
                throw new IOException("httpbingo did not return a valid response " + httpBinResponse.statusCode());
            }

            w.writeStatusLine(HttpStatus.OK);

            Map<String, String> headers = getDefaultHeaders(
                -1, httpBinResponse.headers().map().getOrDefault("Content-Type", List.of("application/json")).getFirst()
            );
            headers.remove("Content-Length");
            headers.put("Transfer-Encoding", "chunked");

            w.writeHeaders(headers);
            byte[] buffer = new byte[1024];
            InputStream in = httpBinResponse.body();
            while (true) {
                int bytesRead = in.read(buffer, 0, buffer.length);

                if (bytesRead == -1) {
                    w.writeChunkedBodyDone();
                    break;
                }
                byte[] chunk = Arrays.copyOfRange(buffer,0, bytesRead);
                w.writeChunkedBody(chunk);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("error creating http response: " + e.getMessage());
        }
    }

    private void ok(ResponseWriter writer, byte[] payload, String contentType) throws IOException {
        writer.writeStatusLine(HttpStatus.OK);
        writer.writeHeaders(getDefaultHeaders(payload.length, contentType));
        writer.writeBody(payload);
    }
}