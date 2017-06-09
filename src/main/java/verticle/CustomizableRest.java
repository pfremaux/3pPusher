package verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import org.apache.commons.lang3.StringUtils;
import thirdpartypusher.db.DbAccessor;
import thirdpartypusher.db.Request;
import thirdpartypusher.start.InputSetupManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class CustomizableRest extends AbstractVerticle {

    private final ConfigCustom config;
    private final Router router;
    Map<String, DbAccessor> dbAccessorMap;

    public CustomizableRest(ConfigCustom config, Router router, Map<String, DbAccessor> dbAccessorMap) {
        this.config = config;
        this.router = router;
        this.dbAccessorMap = dbAccessorMap;
    }

    @Override
    public void start() throws Exception {
        super.start();
        InputSetupManager<HttpServerOptions> optionsManager = new InputSetupManager<>();
        /*ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ConfigCustom config = mapper.readValue(new File("./config/hello.tyaml"), ConfigCustom.class);*/
        final Vertx vertx = getVertx();

        HttpServerOptions options = new HttpServerOptions()
                // .setTrustStoreOptions(options)
                // .setClientAuth(clientAuth)
                ;// .setMaxWebsocketFrameSize(1000000);
        optionsManager.setup(options);
        final HttpServer server = vertx.createHttpServer(options);
        // PusherInit.init(server).requestHandler(router::accept).listen(config.getPort());
        server.requestHandler(router::accept).listen(config.getPort());

        router.get(config.getPath())./*consumes("application/json").*/handler(rc -> {
            //rc.response().setStatusCode(200).end();
            HttpServerRequest request = rc.request();
            Map<String, String>  params = customInput(request);
            customDbActions();
            JsonObject jsonObject = new JsonObject(config.getResponse());
            final String strResponse;
            if (jsonObject.containsKey("txt")) {
                strResponse = responseAsString(jsonObject, params);
            } else if (jsonObject.containsKey("json")) {
                strResponse = StringUtils.EMPTY;
                //strResponse = new JsonObject(jsonObject.getValue("json")).toString();
            } else if (jsonObject.containsKey("yaml")) {
                strResponse = StringUtils.EMPTY;
            } else {
                strResponse = StringUtils.EMPTY;
            }

            customResponse(request, strResponse);
        });

        System.out.println(config.getPath());
    }

    private String responseAsString(JsonObject jsonObject, Map<String, String>  params) {
        String txt = jsonObject.getString("txt");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            txt = txt.replaceAll(":"+entry.getKey(), entry.getValue());
        }
        return txt;
    }

    private Map<String, String>  customInput(HttpServerRequest request) {
        Map<String, String> allInput = new HashMap<>();
        String param = null;
        String uri = request.uri();
        Map<String, String> qParams = extractUrlParam(uri);
        System.out.println("qparam = " + qParams);
        /*for (Map.Entry<String, String> entry : config.getQuery().entrySet()) {
        }*/
        allInput.putAll(qParams);
        for (Map.Entry<String, String> entry : config.getUrlParam().entrySet()) {
            // param .... d'url
            param = request.getParam(entry.getKey());
            // TODO compute if exists
            allInput.put(entry.getKey(), param);
        }
        // TODO merger ce2 sortes dinput. Dans le futur on mappera le body pour les POST et PUT
        return allInput;
    }

    private void customDbActions() {
        for (Map<String, Object> mapAction : config.getActions()) {
            if ("sql".equals(mapAction.get("type"))) {
                Object dataSource = mapAction.get("in");

            }
        }
    }

    private void customResponse(HttpServerRequest request, String strResponse) {
        request.response().putHeader("Content-length", strResponse.length() + "").write(strResponse).end();

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

    private void sql(String in, Map<String, Object> action) throws SQLException {
        Connection inMemory = dbAccessorMap.get(in).connect();
        List params = Arrays.asList("mr", 1);
        Map<String, Class> expRes = new HashMap<>();
        String request = (String) action.get("command");
        action.get("save");
        expRes.put("civ", String.class);
        expRes.put("cnt", String.class);
        /*Request request = new Request(RQT_SELECT, Collections.emptyMap(), expRes);
        dbAccessorMap.get("inMemory").execute(inMemory, RQT_CREATE_TBL_TABLE, Collections.emptyList());
        dbAccessorMap.get("inMemory").execute(inMemory, RQT_INSERT, params);
        Collection<Map<String, Object>> result = dbAccessorMap.get("inMemory").read(inMemory, request, Collections.emptyList());
        dbAccessorMap.get("inMemory").disconnect(inMemory);
        for (Map<String, Object> map : result) {
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }*/
    }

    public static Map<String, String> extractUrlParam(String path) {
        final int i = path.indexOf('?');
        if (i == -1) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        String strQueryParam = path.substring(i+1);
        String[] tabQueryParams = strQueryParam.split("&");
        for (String queryParam : tabQueryParams) {
            if (!queryParam.contains("=")) {
                continue;
            }
            String [] pair = queryParam.split("=");
            result.put(pair[0], pair[1]);
        }
        return result;
    }
}
