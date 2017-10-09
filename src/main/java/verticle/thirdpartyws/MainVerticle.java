package verticle.thirdpartyws;

import io.vertx.core.Vertx;

public class MainVerticle {

    private final Vertx vertx;

    public MainVerticle() {
        vertx = Vertx.vertx();
        vertx.deployVerticle(new VerticleForCustomer());
        vertx.deployVerticle(new VerticleForBrowser());
    }

    public static void main(String... s) {
        new MainVerticle();
    }

    public void close() {
        vertx.deploymentIDs().forEach(vertx::undeploy);
        // vertx.close();
    }

    public Vertx getVertx() {
        return vertx;
    }
}
