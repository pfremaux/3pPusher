package restserver.old.pusher;

import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import restserver.Cache;

public final class PusherInit {

    private PusherInit() {

    }

    public static HttpServer init(HttpServer server) {
        return server.websocketHandler(websocket -> {
            System.out.println("Connected!" + websocket.binaryHandlerID());
            // websocket.writeFinalTextFrame("test");
            //Cache.uuidToWs.put(websocket.binaryHandlerID(), websocket);
            websocket.handler(h -> {
                JsonObject json = h.toJsonObject();
                String id = json.getString("id");
                Cache.uuidToWs.put(id, websocket);
                System.out.println("Pusher> reçu d'un client l'id : " + id);
                Cache.subscriptionsFromClient.add(id);
            });
            websocket.closeHandler(c -> {
                Cache.uuidToWs.remove(websocket.binaryHandlerID());
                System.out.println("Disconnected! Cache size : "
                        + Cache.uuidToWs.size());
            });
        });
    }

}
