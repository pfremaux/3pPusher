package thirdpartypusher.start;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import thirdpartypusher.db.DbAccessor;
import verticle.rest.CustomizableRest;
import verticle.rest.RestDao;
import verticle.rest.RestService;
import verticle.rest.config.ConfigCustom;
import verticle.rest.config.ConfigWatcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StartupRest {

    private final Vertx vertx;

    public StartupRest() {
        this.vertx = Vertx.vertx();
    }

    public static void main(String... s) throws IOException, SQLException {
        new StartupRest().start();
    }

    public void start() throws IOException, SQLException {
        //InputSetupManager optionsManager = new InputSetupManager<>();
        Map<String, DbAccessor> dbAccessorMap = loadBootstrapConfigForDatabase();
        loadRestServices(dbAccessorMap);
    }

    private Map<String, DbAccessor> loadBootstrapConfigForDatabase() throws IOException, SQLException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Map<String, Object> mapConf = mapper.readValue(new File("./bootstrapConfig/database.yaml"), Map.class);
        Map<String, DbAccessor> dbAccessorMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : mapConf.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
            Map<String, Object> dbConf = (Map<String, Object>) entry.getValue();
            String driver = (String) dbConf.get("driver");
            String uri = (String) dbConf.get("uri");
            String login = (String) dbConf.get("login");
            String password = (String) dbConf.get("password");
            DbAccessor dbAccessor = new DbAccessor(driver, uri, login, password);
            dbAccessorMap.put(entry.getKey(), dbAccessor);
            List<String> script = (List<String>) dbConf.get("script");
            Connection inMemory = dbAccessor.connect();
            for (String sqlLine : script) {
                dbAccessor.write(inMemory, sqlLine, Collections.emptyList());
            }
            dbAccessor.disconnect(inMemory);
        }
        return dbAccessorMap;
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
        Map<String, String> filenamePerDeploymentId = new ConcurrentHashMap<>();
        for (File fileConfig : filesConfig) {
            final ConfigCustom config = mapper.readValue(fileConfig, ConfigCustom.class);
            config.validate();
            final String fileConfigName = fileConfig.getName();
            RestDao restDao = new RestDao(dbAccessorMap);
            RestService restService = new RestService(restDao, config);
            vertx.deployVerticle(new CustomizableRest(config, router, restService), handler -> filenamePerDeploymentId.put(fileConfigName, handler.result()));
        }
        prepareAndLaunchWatcher(dbAccessorMap, mapper, router, filenamePerDeploymentId);
    }


    protected void prepareAndLaunchWatcher(Map<String, DbAccessor> dbAccessorMap, ObjectMapper mapper, Router router, Map<String, String> filenamePerDeploymentId) throws IOException {
        // TODO watcher ?
        ConfigWatcher configWatcher = new ConfigWatcher("./config", path -> {
            /*String strPath = path.toString();
            if (strPath.endsWith("yaml")) {
                System.out.println("create " + strPath);
                final ConfigCustom config;
                try {
                    config = mapper.readValue(path.toFile(), ConfigCustom.class);
                    vertx.deployVerticle(new CustomizableRest(config, router, dbAccessorMap, path.toFile().getName()));
                } catch (IOException e) {
                    System.err.println("Error occurent while loading the new version of " + path);
                    e.printStackTrace();
                }
            }*/
        }, path -> {
            String strPath = path.toString();
            if (strPath.endsWith("yaml")) {
                System.out.println("updating " + strPath);
                final ConfigCustom config;
                try {
                    config = mapper.readValue(new File("./config/" + path.toString()), ConfigCustom.class);
                    String deploymentID = filenamePerDeploymentId.remove(path.toFile().getName());
                    /// Don t know why if i don't remove all verticles, undeploy won't work
                    //vertx.deploymentIDs().forEach(e -> {
                    //if (e.equals(deploymentID)) {
                    vertx.undeploy(deploymentID);
                    //}
                    //});
                    //loadRestConfigFiles(dbAccessorMap, mapper);
                    RestDao restDao = new RestDao(dbAccessorMap);
                    RestService restService = new RestService(restDao, config);
                    vertx.deployVerticle(new CustomizableRest(config, router, restService), handler -> filenamePerDeploymentId.put(path.toFile().getName(), handler.result()));
                } catch (IOException e) {
                    System.err.println("Error occurent while loading the new version of " + path);
                    e.printStackTrace();
                }
            }
        }, path -> {
            /*String strPath = path.toString();
            if (strPath.endsWith("yaml")) {
                System.out.println("delete " + strPath);
                vertx.undeploy(path.toFile().getName());
            }*/
        });
        Thread thread = new Thread(configWatcher);
        thread.start();
    }

}
