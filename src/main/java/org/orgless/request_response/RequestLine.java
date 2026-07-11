package org.orgless.request_response;

public record RequestLine(String httpVersion, String requestTarget, HttpMethod method) {

}