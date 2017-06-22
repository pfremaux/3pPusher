package verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
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

        Route route;
        if ("GET".equals(config.getMethod())) {
            route = router.get(config.getPath());
        } else if ("POST".equals(config.getMethod())) {
            route = router.post(config.getPath());
        } else {
            route = null;
        }
        route./*consumes("application/json").*/handler(rc -> {
            //rc.response().setStatusCode(200).end();
            HttpServerRequest request = rc.request();

            Map<String, Object> params = customInput(request);
        });
        System.out.println(config.getPath());
    }

   /* private Map<String, Object> copyMap(Map<String, Object> map) {
        Map<String, Object> objectMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (Map.class.isAssignableFrom(entry.getValue().getClass())) {
                objectMap.put(entry.getKey(), copyMap((Map<String, Object>) entry.getValue()));
            } else {
                objectMap.put(entry.getKey(), entry.getValue());
            }
        }
        return objectMap;
    }*/

    private String responseAsJson(JsonObject jsonObject, Map<String, Object> params) {
        JsonObject json = jsonObject.getJsonObject("json").copy();
        for (Map.Entry<String, Object> entryModelJson : json) {
            if (JsonObject.class.isAssignableFrom(entryModelJson.getValue().getClass())) {
                responseAsJson((JsonObject) entryModelJson.getValue(), params);
            } else if (entryModelJson.getValue().getClass().isAssignableFrom(JsonArray.class)) {
                // responseAsJson((JsonObject) entry.getValue(), params);
            } else {
                if (entryModelJson.getValue().getClass().isAssignableFrom(String.class)) {
                    String valueModelJson = (String) entryModelJson.getValue();
                    Object realValueReturned = null;
                    if (valueModelJson.startsWith(":")) {
                        String correspondingValueModelJson = params.get(valueModelJson.substring(1)).toString();
                        if (correspondingValueModelJson != null) {
                            for (Map.Entry<String, Object> entry2 : params.entrySet()) {
                                if (valueModelJson.equals(":" + entry2.getKey())) {
                                    realValueReturned = entry2.getValue();
                                }/* else {
                                    result = val;
                                }*/
                            }
                        }
                    }
                    entryModelJson.setValue(realValueReturned);
                }
            }
        }
        return json.toString();
    }

    private String responseAsString(JsonObject jsonObject, Map<String, Object> params) {
        String txt = jsonObject.getString("txt");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object o = entry.getValue();
            String value;
            if (Map.class.isAssignableFrom(o.getClass())) {
                value = new JsonObject((Map) o).toString();
            } else {
                value = o.toString();
            }
            txt = txt.replaceAll(":" + entry.getKey(), value);
        }
        return txt;
    }

    private Map<String, Object> customInput(HttpServerRequest request) {

        Map<String, Object> allInput = new HashMap<>();
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

        request.bodyHandler(bh -> {
            // TODO valider en amont si la request expect un body
            if (bh.length() > 0) {
                final JsonObject body = bh.toJsonObject();
                for (Map.Entry<String, Object> entry : body) {
                    allInput.put(entry.getKey(), entry.getValue());
                }
            }
            final String strResponse;
            try {
                customDbActions(allInput);
            } catch (SQLException e) {
                customResponse(request, e.getMessage());
            }
            JsonObject jsonObject = new JsonObject(config.getResponse());
            if (jsonObject.containsKey("txt")) {
                strResponse = responseAsString(jsonObject, allInput);
            } else if (jsonObject.containsKey("json")) {
                //strResponse = StringUtils.EMPTY;
                strResponse = responseAsJson(jsonObject, allInput);
            } else if (jsonObject.containsKey("yaml")) {
                strResponse = StringUtils.EMPTY;
            } else {
                strResponse = StringUtils.EMPTY;
            }

            customResponse(request, strResponse);
        });

        // TODO merger ce2 sortes dinput. Dans le futur on mappera le body pour les POST et PUT
        return allInput;
    }

    private void customDbActions(Map<String, Object> params) throws SQLException {
        for (Map<String, Object> mapAction : config.getActions()) {
            if ("sql".equals(mapAction.get("type"))) {
                String dataSource = (String) mapAction.get("in");
                DbAccessor inMemory = dbAccessorMap.get(dataSource);
                String req = (String) mapAction.get("command");
                if (req.startsWith("select")) {
                    Map<String, String> resultKeys = new HashMap<>();
                    List<String> paramName = (List<String>) mapAction.get("param");
                    List inputQuery = new ArrayList();
                    if (paramName != null) {
                        for (String s : paramName) {
                            Object o = params.get(s);
                            inputQuery.add(o);
                        }
                    }
                    resultKeys.put("nom", String.class.getName());
                    resultKeys.put("civ", String.class.getName());
                    Request request = new Request(req, Collections.emptyMap(), resultKeys);
                    JsonArray read = inMemory.read(inMemory.connect(), request, inputQuery);
                    String save = (String) mapAction.get("save");
                    params.put(save, read);
                } else if (req.startsWith("insert")) {
                    // params
                    List<String> lst = (List<String>) mapAction.get("param");
                    List reqParam = new ArrayList<>();
                    for (String key : lst) {
                        reqParam.add(params.get(key));
                    }
                    inMemory.execute(inMemory.connect(), req, reqParam);
                    String save = (String) mapAction.get("save");
                    params.put(save, "TODOreturnId");
                }
            }
        }
    }

    private void customResponse(HttpServerRequest request, String strResponse) {
        request.response().endHandler(h -> {
            System.out.println(h);
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
}
