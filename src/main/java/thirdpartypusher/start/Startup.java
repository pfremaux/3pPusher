package thirdpartypusher.start;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import thirdpartypusher.db.DbAccessor;
import thirdpartypusher.db.Request;
import verticle.ConfigCustom;
import verticle.CustomizableRest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class Startup {

    private final Vertx vertx;

    public Startup() {
        this.vertx = Vertx.vertx();
    }

    public static void main(String... s) throws IOException, SQLException {
        new Startup().start();
    }

    private final String RQT_CREATE_TBL = "CREATE TABLE personnes ( id INTEGER IDENTITY, nom VARCHAR(32), prenom VARCHAR(32))";
    private final String RQT_CREATE_TBL_TABLE = "CREATE TABLE tbl ( id INTEGER IDENTITY, cnt INTEGER default 0, civ VARCHAR(32), nom VARCHAR(32))";
    private final String RQT_INSERT = "insert into tbl (civ, cnt) values (?, ?);";
    private final String RQT_SELECT = "SELECT * FROM tbl";

    public void start() throws IOException, SQLException {
        //InputSetupManager optionsManager = new InputSetupManager<>();
        Map<String, DbAccessor> dbAccessorMap = loadBootstrapConfig();

        customInit(dbAccessorMap);
        loadRestServices(dbAccessorMap);
    }


    private Map<String, DbAccessor> loadBootstrapConfig() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map<String, Object> mapConf = mapper.readValue(new File("./bootstrapConfig/database.yaml"), Map.class);
        Map<String, DbAccessor> dbAccessorMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : mapConf.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
            Map<String, String> dbConf = (Map<String, String>) entry.getValue();
            String driver = dbConf.get("driver");
            String uri = dbConf.get("uri");
            String login = dbConf.get("login");
            String password = dbConf.get("password");
            DbAccessor dbAccessor = new DbAccessor(driver, uri, login, password);
            dbAccessorMap.put(entry.getKey(), dbAccessor);
        }
        return dbAccessorMap;
    }

    private void customInit(Map<String, DbAccessor> dbAccessorMap) throws SQLException {
        Connection inMemory = dbAccessorMap.get("inMemory").connect();
        List params = Arrays.asList("mr", 1);
        Map<String, String> expRes = new HashMap<>();
        expRes.put("civ", String.class.getName());
        expRes.put("cnt", String.class.getName());
        Request request = new Request(RQT_SELECT, Collections.emptyMap(), expRes);
        dbAccessorMap.get("inMemory").execute(inMemory, RQT_CREATE_TBL_TABLE, Collections.emptyList());
        dbAccessorMap.get("inMemory").execute(inMemory, RQT_INSERT, params);
        JsonArray result = dbAccessorMap.get("inMemory").read(inMemory, request, Collections.emptyList());
        dbAccessorMap.get("inMemory").disconnect(inMemory);
        for (Object o : result) {
            JsonObject map = (JsonObject) o;
            for (Map.Entry<String, Object> entry : map) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    /* ex
* String rule1 = "int[0..9999],null";
   String rule2 = "string[0..9999]";
   String rule3 = "string/[a-z]/,null";"
*/
    private static String convertRule(String rule) {
        if (rule.startsWith("int")) {

        } else if (rule.startsWith("string")) {
            String validation = rule.substring("string".length());
            String[] diffValidations = validation.split(",");
            for (int i = 0; i < diffValidations.length; i++) {
                if (diffValidations[i].startsWith("/")) {

                }
            }
        }
        return "";
    }

    private void loadRestServices(Map<String, DbAccessor> dbAccessorMap) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final File configDirectory = Paths.get("./config").toFile();
        File[] filesConfig = configDirectory.listFiles((dir, name) -> name.endsWith(".yaml"));
        final Router router = Router.router(vertx);
        for (File fileConfig : filesConfig) {
            final ConfigCustom config = mapper.readValue(fileConfig, ConfigCustom.class);
            vertx.deployVerticle(new CustomizableRest(config, router, dbAccessorMap));
        }
    }

}
