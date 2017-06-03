package thirdpartypusher;

import io.vertx.core.http.ServerWebSocket;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Cache {
    private Cache() {

    }

    public static Set<String> subscriptionsFromCustomer = new HashSet<>();
    public static Set<String> subscriptionsFromClient = new HashSet<>();

    public static boolean subscriptionClient(String id) {
        if (subscriptionsFromCustomer.contains(id)) {
            subscriptionsFromCustomer.remove(id);
            return true;
        } else {
            subscriptionsFromClient.add(id);
            return false;
        }
    }

    public static boolean subscriptionCustomer(String id) {
        if (subscriptionsFromClient.contains(id)) {
            subscriptionsFromClient.remove(id);
            return true;
        } else {
            subscriptionsFromCustomer.add(id);
            return false;
        }
    }


    public static Map<String, String> UuidToId = new HashMap<>();
    public static Map<String, ServerWebSocket> uuidToWs = new HashMap<>();
    public static Map<String, ServerWebSocket> idToWs = new HashMap<>();

}
