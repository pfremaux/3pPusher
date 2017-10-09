package verticle.thirdpartyws;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.json.JsonObject;
import restserver.Cache;

public class VerticleForBrowser extends AbstractVerticle {

    public static final int PORT = 8080;

    HttpServer httpServer;

    @Override
    public void start() throws Exception {
        super.start();
        httpServer = vertx.createHttpServer().websocketHandler(serverWebSocket -> {
            registerNewClient(serverWebSocket);
            serverWebSocket.handler(buffer -> {
                // buffer.toJsonObject();
                manageMessageReceived(serverWebSocket, buffer.toJsonObject());
            });
            serverWebSocket.closeHandler(closeH -> {
                String id = Cache.UuidToId.remove(serverWebSocket.textHandlerID());
                System.out.println("Closing connection for " + id);
                Cache.idToWs.remove(id);
            });
        }).listen(PORT);
    }

    // par defaut le client ne devrait pas pouvoir pusher de msg dans un premier temps
    public void manageMessageReceived(ServerWebSocket serverWebSocket, JsonObject message) {
        // subscribe avec un code transmis par oldServeur
        final String action = message.getString("action");
        if ("subscribe".equalsIgnoreCase(action)) {
            final String id = message.getString("id");
            Cache.UuidToId.put(serverWebSocket.textHandlerID(), id);
            System.out.println("add id to cache : " + id);
            Cache.idToWs.put(id, serverWebSocket);
        }
    }

    public void registerNewClient(ServerWebSocket serverWebSocket) {
        Cache.uuidToWs.put(serverWebSocket.textHandlerID(), serverWebSocket);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        httpServer.close();
    }

}
