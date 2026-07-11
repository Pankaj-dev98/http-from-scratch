package org.orgless.server;

import org.orgless.request_response.HttpRequest;
import org.orgless.response.ResponseWriter;

@FunctionalInterface
public interface Handler {
    void handle(ResponseWriter w, HttpRequest httpRequest) throws Exception;
}