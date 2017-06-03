package test.ws.fakecustomer;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;

import java.util.concurrent.CountDownLatch;

public final class OldServerWeb {
    public static final String URL = "http://localhost:8080/add";
    private static CountDownLatch messageLatch;
    public static final String SUBSCRIBE = "{\"action\":\"subscribe\", \"id\":\"monuuid\"}";
    public static final String SENT_MESSAGE = "{\"action\":\"push\", \"id\":\"monuuid\", \"data\":{\"msg\":\"cool !\"}}";

    final Vertx vertx;
    final HttpClient httpClient;

    public OldServerWeb(Vertx vertx) {
        this.vertx = vertx;
        httpClient = vertx.createHttpClient();
    }

    public static void main(String[] args) {


        /*HttpClientRequest httpClientRequest = httpClient.request(HttpMethod.POST, 8080, "localhost", "/");
        httpClientRequest.write("{'newClient':'UUID'}")
                .handler(response -> {
                    System.out.println(response.statusCode());
                }).end();*/
    }


    public void makeCall(String host, int port, String path, String msg) {
        System.out.println(String.format("calling %s:%d%s \nwith payload : %s", host, port, path, msg));
        httpClient.post(port, host, path, rh -> System.out.println(this.getClass().getSimpleName() + ".makeCall : " + rh.statusCode()))
                /*.putHeader("content-type", "application/json")
                .putHeader("Content-length", msg.length() + "")*/.end(msg);

    }

    public void close() {
        httpClient.close();
//        vertx.close();
    }

}