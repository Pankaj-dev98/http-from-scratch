package org.orgless;

import org.orgless.server.Server;

import java.util.concurrent.CountDownLatch;

public class Main {
    public static void main(String[] args) {
        int port = resolvePort();
        try {
            Server server = Server.serve(port);
            // A CountDownLatch is a thread-safe way to make our main thread "wait"
            CountDownLatch stopLatch = new CountDownLatch(1);

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    server.close();
                    stopLatch.countDown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
            stopLatch.await();
            System.out.println("Server gracefully stopped");
        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            System.exit(1);
        }
    }

    private static int resolvePort() {
        String portValue = System.getenv("PORT");
        if (portValue == null || portValue.isBlank()) {
            return 42069;
        }

        try {
            return Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid PORT value: " + portValue, e);
        }
    }
}
