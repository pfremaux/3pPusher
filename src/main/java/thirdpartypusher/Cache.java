package thirdpartypusher;

import io.vertx.core.http.ServerWebSocket;

import java.util.HashMap;
import java.util.Map;

public final class Cache {
    private Cache() {

    }

    public static Map<String, ServerWebSocket> p = new HashMap<>();
}
