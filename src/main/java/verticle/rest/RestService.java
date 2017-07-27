package verticle.rest;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import verticle.rest.config.ConfigCustom;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

public class RestService {

    private final static Logger LOGGER = Logger.getLogger(RestService.class.getName());

    public static final String VARIABLE_PREFIX = ":";

    public enum SUPPORTED_OUTPUT_FORMAT {
        TXT("txt"), JSON("json"), YAML("yaml");
        private String format;

        SUPPORTED_OUTPUT_FORMAT(String val) {
            format = val;
        }

        String format() {
            return format;
        }
    }

    public enum SUPPORTED_ACTION {
        SQL("sql"), TRANSFORM("transform");
        private String format;

        SUPPORTED_ACTION(String val) {
            format = val;
        }

        String format() {
            return format;
        }
    }

    private RestDao restDao;
    private final ConfigCustom config;

    public RestService(RestDao restDao, ConfigCustom config) {
        this.restDao = restDao;
        this.config = config;
    }

    public String buildFormattedResponse(Map<String, Object> allInput) {
        String strResponse;
        JsonObject jsonObject = new JsonObject(config.getResponse());
        if (jsonObject.containsKey(SUPPORTED_OUTPUT_FORMAT.TXT.format())) {
            strResponse = responseAsString(jsonObject, allInput);
        } else if (jsonObject.containsKey(SUPPORTED_OUTPUT_FORMAT.JSON.format())) {
            strResponse = responseAsJson(jsonObject, allInput);
        } else if (jsonObject.containsKey(SUPPORTED_OUTPUT_FORMAT.YAML.format())) {
            strResponse = StringUtils.EMPTY;
        } else {
            strResponse = StringUtils.EMPTY;
        }
        return strResponse;
    }

    public void customActions(Map<String, Object> IOParams) throws SQLException {
        for (Map<String, Object> mapActionsToDo : config.getActions()) {
            if (SUPPORTED_ACTION.SQL.format().equals(mapActionsToDo.get("type"))) {
                restDao.customDbAction(IOParams, mapActionsToDo);
            } else if (SUPPORTED_ACTION.TRANSFORM.format().equals(mapActionsToDo.get("type"))) {
                // TODO update data
            }
        }
    }

    private String responseAsJson(JsonObject jsonObject, Map<String, Object> params) {
        JsonObject json = jsonObject.getJsonObject(SUPPORTED_OUTPUT_FORMAT.JSON.format()).copy();
        for (Map.Entry<String, Object> entryModelJson : json) {
            if (JsonObject.class.isAssignableFrom(entryModelJson.getValue().getClass())) {
                responseAsJson((JsonObject) entryModelJson.getValue(), params);
            } else if (entryModelJson.getValue().getClass().isAssignableFrom(JsonArray.class)) {
                // responseAsJson((JsonObject) entry.getValue(), params);
            } else {
                if (entryModelJson.getValue().getClass().isAssignableFrom(String.class)) {
                    String valueModelJson = (String) entryModelJson.getValue();
                    Object realValueReturned = null;
                    if (valueModelJson.startsWith(VARIABLE_PREFIX)) {
                        // TODO revoir l'usage et l'utilité des variables ici
                        Object value = params.get(valueModelJson.substring(1));
                        if (value != null) {
                            for (Map.Entry<String, Object> entry2 : params.entrySet()) {
                                if (valueModelJson.equals(VARIABLE_PREFIX + entry2.getKey())) {
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
        String txt = jsonObject.getString(SUPPORTED_OUTPUT_FORMAT.TXT.format());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object o = entry.getValue();
            String value;
            if (Map.class.isAssignableFrom(o.getClass())) {
                value = new JsonObject((Map) o).toString();
            } else {
                value = o.toString();
            }
            txt = txt.replaceAll(VARIABLE_PREFIX + entry.getKey(), value);
        }
        return txt;
    }

}
