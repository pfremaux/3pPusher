package verticle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import verticle.rest.config.ConfigCustom;

import java.io.File;
import java.io.IOException;

public class MainCustom {

    Vertx vertx;

    public MainCustom() throws IOException {
        vertx = Vertx.vertx();
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ConfigCustom config = mapper.readValue(new File("./config/hello.tyaml"), ConfigCustom.class);
        final Router router = Router.router(vertx);

        //vertx.deployVerticle(new WebService(config, router, Collections.emptyMap(), restDao));
    }

    public static void main(String... s) throws IOException {
        new MainCustom();
    }

    public void close() {
        vertx.deploymentIDs().forEach(vertx::undeploy);
        // vertx.close();
    }

    public Vertx getVertx() {
        return vertx;
    }

}
