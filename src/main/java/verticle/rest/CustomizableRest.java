package verticle.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import logger.SimpleLogFormat;
import thirdpartypusher.start.InputSetupManager;
import verticle.rest.config.ConfigCustom;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CustomizableRest extends AbstractVerticle {

    private final static Logger LOGGER = Logger.getLogger(CustomizableRest.class.getName());
    private boolean prod = false;

    private final ConfigCustom config;
    private final Router router;

    private RestService restService;

    public CustomizableRest(ConfigCustom config, Router router, RestService restService) {
        this.config = config;
        this.router = router;
        this.restService = restService;
    }

    @Override
    public void start() throws Exception {
        super.start();
        LOGGER.setLevel(Level.ALL);

        FileHandler fileTxt = new FileHandler("rest-services.log");
        //ConsoleHandler consoleHandler = new ConsoleHandler();
        fileTxt.setFormatter(new SimpleLogFormat());
        LOGGER.addHandler(fileTxt);

        Handler[] handlers = LOGGER.getHandlers();
        // TODO little dangerous ? should separate vertx logger from my code
       /* if (handlers[0] instanceof ConsoleHandler) {
            handlers[0].setFormatter(new SimpleLogFormat());
            //LOGGER.removeHandler(handlers[0]);
        }*/
        LOGGER.info("Loading verticle " + CustomizableRest.class.getSimpleName());
        InputSetupManager<HttpServerOptions> optionsManager = new InputSetupManager<>();
        final Vertx vertx = getVertx();

        // Global setup
        HttpServerOptions options = new HttpServerOptions()
                // .setTrustStoreOptions(options)
                // .setClientAuth(clientAuth)
                ;// .setMaxWebsocketFrameSize(1000000);
        optionsManager.setup(options);
        final HttpServer server = vertx.createHttpServer(options);
        server.requestHandler(router::accept).listen(config.getPort());

        Route route;
        if (HttpMethod.GET.name().equals(config.getMethod())) {
            route = router.get(config.getPath());
        } else if (HttpMethod.POST.name().equals(config.getMethod())) {
            route = router.post(config.getPath());
        } else if (HttpMethod.DELETE.name().equals(config.getMethod())) {
            route = router.delete(config.getPath());
        } else if (HttpMethod.PUT.name().equals(config.getMethod())) {
            route = router.put(config.getPath());
        } else {
            throw new RuntimeException("Unknown HTTP method " + config.getMethod() + " configuration file.");
        }
        route./*consumes("application/json").*/handler(rc -> {
            //rc.response().setStatusCode(200).end();
            HttpServerRequest request = rc.request();

            consumeRequest(request);

        });
        LOGGER.info("Verticle " + CustomizableRest.class.getSimpleName() + " loaded : " + config.toSimpleString());
    }


    private Map<String, Object> consumeRequest(HttpServerRequest request) {
        Map<String, Object> allInput = new HashMap<>();
        String urlParam;
        String uri = request.uri();
        Map<String, String> queryParams = extractQueryParam(uri);
        LOGGER.finest(() -> "qparam = " + queryParams);
        allInput.putAll(queryParams);
        for (Map.Entry<String, String> entry : config.getUrlParam().entrySet()) {
            urlParam = request.getParam(entry.getKey());
            allInput.put(entry.getKey(), urlParam);
        }
        request.bodyHandler(bh -> {
            // TODO valider en amont si la request expect un body
            // Map input request
            if (bh.length() > 0) {
                final JsonObject body = bh.toJsonObject();
                for (Map.Entry<String, Object> entry : body) {
                    allInput.put(entry.getKey(), entry.getValue());
                }
            }
            final String strResponse;
            // do actions
            try {
                restService.customActions(allInput);
                // Prepare response
                strResponse = restService.buildFormattedResponse(allInput);
                customResponse(request, 200, strResponse);
            } catch (SQLException e) {
                LOGGER.severe(e.getMessage() + " II SQL state : " + e.getSQLState());
                String returnedMessage = "error";
                if (!prod) {
                    returnedMessage = e.getMessage();
                }
                customResponse(request, 500, returnedMessage);
            }

        });
        return allInput;
    }


    private Map<String, String> extractQueryParam(String path) {
        final int i = path.indexOf('?');
        if (i == -1) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        String strQueryParam = path.substring(i + 1);
        String[] tabQueryParams = strQueryParam.split("&");
        for (String queryParam : tabQueryParams) {
            if (!queryParam.contains("=")) {
                continue;
            }
            String[] pair = queryParam.split("=");
            result.put(pair[0], pair[1]);
        }
        return result;
    }


    private void customResponse(HttpServerRequest request, int statusCode, String strResponse) {
        request.response().setStatusCode(statusCode).endHandler(h -> {
            System.out.println("customResponse : " + h);
        }).putHeader("Content-length", strResponse.length() + "").write(strResponse).end();
            /*JsonObject obj = h.toJsonObject();
            String id = obj.getString("id");
            System.out.println("Pusher> reçu du Customer l'id " + id);
            Cache.subscriptionsFromCustomer.add(id);*/
            /*
                rc.response().setStatusCode(200).end();
            });
            /*HttpServerRequest request = rc.request();
            String uri = request.uri();
            System.out.println(uri);
            request.handler(h -> {
            /*JsonObject obj = h.toJsonObject();
            String id = obj.getString("id");
            System.out.println("Pusher> reçu du Customer l'id " + id);
            Cache.subscriptionsFromCustomer.add(id);*//*
                rc.response().setStatusCode(200).end();
            });*/
    }

}
