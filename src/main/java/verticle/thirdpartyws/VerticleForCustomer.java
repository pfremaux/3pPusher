package verticle.thirdpartyws;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import thirdpartypusher.Cache;

public class VerticleForCustomer extends AbstractVerticle {

    public static final int PORT = 8081;

    HttpServer httpServer;

    @Override
    public void start() throws Exception {
        super.start();
        httpServer = vertx.createHttpServer().requestHandler(handler -> {
            if (HttpMethod.POST.equals(handler.method())) {
                // TODO gerer le cas ou aucun payload n'est envoyé
                handler.handler(buffer -> {
                    final JsonObject jsonObject = buffer.toJsonObject();
                    int status = manageMessageReceived(jsonObject);
                    handler.response().setStatusCode(status).end();
                });

            }

        }).listen(PORT);
    }

    public int manageMessageReceived(JsonObject message) {
        // subscribe avec un code transmis par oldServeur
        final String action = message.getString("action");
        if ("subscribe".equalsIgnoreCase(action)) {
            final String id = message.getString("id");
            System.out.println("subscription alors que deja connu ? " + Cache.subscriptionCustomer(id));
            return 200;
        } else if ("push".equalsIgnoreCase(action)) {
            final String id = message.getString("id");
            final JsonObject data = message.getJsonObject("data");
            if (Cache.idToWs.containsKey(id)) {// TODO optim du cache
                if (data == null || StringUtils.isEmpty(data.toString())) {
                    Cache.idToWs.get(id).writeTextMessage(StringUtils.EMPTY);
                } else {
                    Cache.idToWs.get(id).writeTextMessage(data.toString());
                }
                return 200;
            } else {
                return 400;
            }
        }
        return 405;
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        httpServer.close();
    }
}
