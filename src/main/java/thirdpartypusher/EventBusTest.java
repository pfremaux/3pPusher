package thirdpartypusher;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;

public class EventBusTest {

	/*
	 * { "mode":"1", "data":{"msg":"Hey !"}, "dest":"uuid" }
	 */

	public static void main(String... a) {
		Vertx vertx = Vertx.vertx();
		EventBus eventBus = vertx.eventBus();
		//eventBus.
		Router router = Router.router(vertx);

		HttpServerOptions options = new HttpServerOptions()
		// .setTrustStoreOptions(options)
		// .setClientAuth(clientAuth)
		;// .setMaxWebsocketFrameSize(1000000);
		HttpServer server = vertx.createHttpServer(options);
		server.websocketHandler(websocket -> {
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

			}).requestHandler(router::accept).listen(8080);

		// router.route().handler(BodyHandler.create());
		router.post("/").consumes("application/json").handler(rc -> {
			// rc.request().setExpectMultipart(true);
		
				rc.request().handler(h -> {
					JsonObject obj = h.toJsonObject();
					System.out.println("jsonObject " + obj);
					String dest = obj.getString("dest");
					JsonObject data = obj.getJsonObject("data");
					Cache.p.get(dest).writeFinalTextFrame(data.toString());
				});
				rc.next();
			});
	}

}

