package thirdpartypusher.pusher;

import io.vertx.core.http.HttpServer;
import thirdpartypusher.Cache;

public final class PusherInit {

    private PusherInit() {

    }

    public static HttpServer init(HttpServer server) {
        return server.websocketHandler(websocket -> {
            System.out.println("Connected!" + websocket.binaryHandlerID());
            // websocket.writeFinalTextFrame("test");
            Cache.p.put(websocket.binaryHandlerID(), websocket);
            websocket.closeHandler(c -> {
                // logger.debug("Websocket {} closed.",
                // webSocket.binaryHandlerID());
                Cache.p.remove(websocket.binaryHandlerID());
                System.out.println("Disconnected! Cache size : "
                        + Cache.p.size());
                // consumer.unregister();
            });

        });
    }

}
