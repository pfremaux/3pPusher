package restserver.start;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import restserver.db.DbAccessor;
import verticle.rest.RestDao;
import verticle.rest.RestService;
import verticle.rest.WebService;
import verticle.rest.config.ConfigCustom;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StartupRest {

    private final Vertx vertx;

    private StartupRest() {
        this.vertx = Vertx.vertx();
    }

    public static void main(String... s) throws IOException, SQLException {
        new StartupRest().start();
    }

    private void start() throws IOException, SQLException {
        //InputSetupManager optionsManager = new InputSetupManager<>();
        Map<String, DbAccessor> dbAccessorMap = loadConfigForDatabase();
        loadWebServices(dbAccessorMap);
    }

    private Map<String, DbAccessor> loadConfigForDatabase() throws IOException, SQLException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final JavaType mapObjectPerString = mapper.getTypeFactory().constructParametricType(Map.class, String.class, Object.class);
        final Map<String, Object> mapConf = mapper.readValue(new File("./bootstrapConfig/database.yaml"), mapObjectPerString);
        final Map<String, DbAccessor> dbAccessorMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : mapConf.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
            final Map<String, Object> dbConf = (Map<String, Object>) entry.getValue();
            final String driver = (String) dbConf.get("driver");
            final String uri = (String) dbConf.get("uri");
            final String login = (String) dbConf.get("login");
            final String password = (String) dbConf.get("password");
            final DbAccessor dbAccessor = new DbAccessor(driver, uri, login, password);
            dbAccessorMap.put(entry.getKey(), dbAccessor);
            final List<String> script = (List<String>) dbConf.get("script");
            final Connection inMemory = dbAccessor.connect();
            for (String sqlLine : script) {
                dbAccessor.writeAndGetNumberUpdated(inMemory, sqlLine, Collections.emptyMap(), Collections.emptyMap());
            }
            dbAccessor.disconnect(inMemory);
        }
        return dbAccessorMap;
    }

    /**
     * ex
     * String rule1 = "int[0..9999],null";
     * String rule2 = "string[0..9999]";
     * String rule3 = "string/[a-z]/,null";"
     */
    private static String convertRule(String rule) {
        if (rule.startsWith("Integer")) {

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

    private void loadWebServices(Map<String, DbAccessor> dbAccessorMap) throws IOException {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        final File configDirectory = Paths.get("./config").toFile();
        final File[] filesConfig = configDirectory.listFiles((dir, name) -> name.endsWith(".yaml"));
        final Router router = Router.router(vertx);
        if (filesConfig == null) {
            throw new RuntimeException("Config directory " + configDirectory + " is not valid.");
        }
        for (File fileConfig : filesConfig) {
            final ConfigCustom config = mapper.readValue(fileConfig, ConfigCustom.class);
            config.validate();
            final RestDao restDao = new RestDao(dbAccessorMap);
            final RestService restService = new RestService(restDao, config);
            vertx.deployVerticle(new WebService(config, router, restService));
        }
    }

}
