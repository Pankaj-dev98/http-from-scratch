package org.orgless.server;

import org.orgless.parsers.RequestParser;
import org.orgless.request_response.HttpRequest;
import org.orgless.response.HttpStatus;
import org.orgless.response.ResponseWriter;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class Server {

    private final ServerSocket serverSocket;

    private final AtomicBoolean isClosed;

    private final Map<String, Handler> router;

    private Server(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.isClosed = new AtomicBoolean(false);
        this.router = UriMapper.getRoutes();
    }

    public static Server serve(int port) throws IOException {
        Server server = new Server(port);
        Thread.ofVirtual().start(server::listen);
        return server;
    }

    private void listen() {
        System.out.println("starting http server on port " + serverSocket.getLocalPort());
        System.out.println("accepting requests with http versions: [1.1]");

        while (!this.isClosed.get()) {
            try {
                Socket conn = serverSocket.accept();
                Thread.ofVirtual().start(
                    () -> handleRequest(conn)
                );
            } catch (IOException e) {
                if (this.isClosed.get()) {
                    break;
                }
                System.err.println("Accept error: " + e.getMessage());
            }
        }
    }

    private void handleRequest(Socket conn) {
        try (conn) {
            OutputStream out = conn.getOutputStream();
            ResponseWriter writer = new ResponseWriter(out);

            HttpRequest request;
            InputStream in = conn.getInputStream();
            request = RequestParser.parseStream(in);

            String endpoint = request.getRequestLine().requestTarget();
            if (!router.containsKey(endpoint)) {
                sendHtmlError(writer, HttpStatus.NOT_FOUND, "<html><body><h1>404 Not Found</h1></body></html>");
            } else {
                router.get(endpoint).handle(writer, request);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Fatal connection error: " + e.getMessage());
        }
    }

    private void sendHtmlError(ResponseWriter w, HttpStatus status, String htmlBody) throws IOException {
        byte[] bodyBytes = htmlBody.getBytes(StandardCharsets.UTF_8);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "text/html");
        headers.put("Content-Length", String.valueOf(bodyBytes.length));
        headers.put("Connection", "close");

        w.writeStatusLine(status);
        w.writeHeaders(headers);
        w.writeBody(bodyBytes);
    }

    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            serverSocket.close();
        }
    }

//    static void main() {
//        System.out.println("abcd");
//    }
}