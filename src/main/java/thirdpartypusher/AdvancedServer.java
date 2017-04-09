package thirdpartypusher;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import thirdpartypusher.pusher.PusherInit;

/**
 * Server websocket : ws://localhost:8080
 * Server http : http://localhost:8080/ (POST json)
 */
public final class AdvancedServer {

    private final static int LISTENING_PORT = 8080;

	/*
     * { "mode":"1", "data":{"msg":"Hey !"}, "dest":"uuid" }
	 */

    public static void main(String... a) {
        final Vertx vertx = Vertx.vertx();
        final Router router = Router.router(vertx);

        HttpServerOptions options = new HttpServerOptions()
                // .setTrustStoreOptions(options)
                // .setClientAuth(clientAuth)
                ;// .setMaxWebsocketFrameSize(1000000);
        final HttpServer server = vertx.createHttpServer(options);
        PusherInit.init(server).requestHandler(router::accept).listen(LISTENING_PORT);

        // router.route().handler(BodyHandler.create());
        router.post("/").consumes("application/json").handler(rc -> rc.request().handler(h -> {
            JsonObject obj = h.toJsonObject();
            System.out.println("jsonObject " + obj);
            String dest = obj.getString("dest");
            JsonObject data = obj.getJsonObject("data");
            ServerWebSocket webSocketClient = Cache.p.get(dest);
            if (webSocketClient == null) {
                rc.response().setStatusCode(403).end();
            } else {
                webSocketClient.writeFinalTextFrame(data.toString());
                rc.response().setStatusCode(200).end();
            }
        }));
    }
}

