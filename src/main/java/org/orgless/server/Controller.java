package org.orgless.server;

import org.orgless.request_response.HttpRequest;
import org.orgless.response.HttpStatus;
import org.orgless.response.ResponseWriter;
import org.orgless.services.FunFactService;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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

    private void ok(ResponseWriter writer, byte[] payload, String contentType) throws IOException {
        writer.writeStatusLine(HttpStatus.OK);
        writer.writeHeaders(getDefaultHeaders(payload.length, contentType));
        writer.writeBody(payload);
    }
}