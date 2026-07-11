package org.orgless.server;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UriMapper {

    @Getter
    private static final Map<String, Handler> routes = new HashMap<>();

    static {
        initializeRoutes();
    }

    private UriMapper() {
        initializeRoutes();
    }

    public UriMapper(Map<String, Handler> routes) {
        this();
        for (var e : routes.entrySet()) {
            setRoute(e.getKey(), e.getValue());
        }
    }

    public static void setRoute(String endpoint, Handler handler) {
        Objects.requireNonNull(endpoint);
        routes.put(endpoint, handler);
    }

    private static void initializeRoutes() {
        Controller controller = new Controller();
        routes.put("/", controller::home);
        routes.put("/funfact", controller::getFunFact);
    }
}