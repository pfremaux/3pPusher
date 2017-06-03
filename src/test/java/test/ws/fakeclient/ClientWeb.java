package test.ws.fakeclient;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.tyrus.client.ClientManager;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

public final class ClientWeb {
    private static CountDownLatch messageLatch;
    private static final String SUBSCRIBE = "{\"action\":\"subscribe\", \"id\":\"monuuid\"}";
    private static final String SENT_MESSAGE = "{\"action\":\"push\", \"id\":\"monuuid\", \"data\":\"cool !\"}";

    Session sess;
    final Vertx vertx;
    HttpClient httpClient;

    public ClientWeb(Vertx vertx) {
        this.vertx = vertx;
    }

    public static void main(String[] args) {

    }

    public void connect(String uri) {
        try {
            messageLatch = new CountDownLatch(1);

            final ClientEndpointConfig cec = ClientEndpointConfig.Builder.create().build();

            ClientManager client = ClientManager.createClient();
            if (StringUtils.isBlank(uri)) {
                uri = "ws://localhost:8080/";
            }


            sess = client.connectToServer(new Endpoint() {

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    try {
                        session.addMessageHandler((MessageHandler.Whole<String>) message -> {
                            System.out.println("Received message: " + message);
                            messageLatch.countDown();
                        });
                        session.getBasicRemote().sendText(SUBSCRIBE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }, cec, new URI(uri));
            // sess.close();
            //session.getBasicRemote().sendText();
            //messageLatch.await(60, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect2(String host, int port, String path) {
        httpClient = vertx.createHttpClient();
        httpClient.websocket(port, host, path, ws -> {
            ws.handler(buff -> System.out.println("received :" + buff.toString()));
            ws.writeTextMessage(SUBSCRIBE);
        });
    }

    public void close() throws IOException {
        //sess.close();
        httpClient.close();
//        vertx.close();
    }

}