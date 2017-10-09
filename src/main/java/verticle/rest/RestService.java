package verticle.rest;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringUtils;
import verticle.rest.config.ConfigCustom;
import verticle.rest.config.Validator;

import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

public final class RestService {

    private final static Logger LOGGER = Logger.getLogger(RestService.class.getName());

    private static final String VARIABLE_PREFIX = ":";

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

    private final RestDao restDao;
    private final ConfigCustom config;

    public RestService(RestDao restDao, ConfigCustom config) {
        this.restDao = restDao;
        this.config = config;
    }

    public String buildFormattedResponse(Map<String, Object> allInput) {
        final String strResponse;
        final JsonObject jsonObject = new JsonObject(config.getResponse());
        if (jsonObject.containsKey(SUPPORTED_OUTPUT_FORMAT.TXT.format())) {
            strResponse = prepareResponseAsString(jsonObject, allInput);
        } else if (jsonObject.containsKey(SUPPORTED_OUTPUT_FORMAT.JSON.format())) {
            strResponse = prepareResponseAsJson(jsonObject, allInput);
        } else if (jsonObject.containsKey(SUPPORTED_OUTPUT_FORMAT.YAML.format())) {
            strResponse = StringUtils.EMPTY;
        } else {
            strResponse = StringUtils.EMPTY;
        }
        return strResponse;
    }

    public void executeCustomActions(Map<String, Object> IOParams) throws SQLException, BadInputException {
        // Validates inputs
        final Map<String, Validator> validators = config.getValidators();
        for (Map.Entry<String, Validator> validatorPerInputKey : validators.entrySet()) {
            final Object inputValue = IOParams.get(validatorPerInputKey.getKey());
            final Validator validator = validatorPerInputKey.getValue();
            if (!validator.isElligible(inputValue)) {
                throw new BadInputException(inputValue + " in parameter named " + validatorPerInputKey.getKey() + " type mismatch.", validator);
            }
            final Object convertObject = validator.convertObject(inputValue);
            if (!validator.isValid(convertObject)) {
                throw new BadInputException(inputValue + " parameter not permitted.", validator);
            }
        }

        for (Map<String, Object> mapActionsToDo : config.getActions()) {
            if (SUPPORTED_ACTION.SQL.format().equals(mapActionsToDo.get("type"))) {
                restDao.customDbAction(IOParams, mapActionsToDo, config.getKnownTypes());
            } else if (SUPPORTED_ACTION.TRANSFORM.format().equals(mapActionsToDo.get("type"))) {
                // TODO update data
            }
        }
    }

    public void oexecuteCustomActions(Map<String, Object> IOParams) throws SQLException, BadInputException {
        // Validates inputs
        for (Map.Entry<String, Object> entryParameters : IOParams.entrySet()) {
            final Validator validator = config.getValidators().get(entryParameters.getKey());
            if (validator != null) {
                final Object objValue = entryParameters.getValue();
                if (!validator.isElligible(objValue)) {
                    throw new BadInputException(objValue + " type mismatch.", validator);
                }
                final Object convertObject = validator.convertObject(objValue);
                if (!validator.isValid(convertObject)) {
                    throw new BadInputException(objValue + " parameter not permitted.", validator);
                }
            }
        }
        for (Map<String, Object> mapActionsToDo : config.getActions()) {
            if (SUPPORTED_ACTION.SQL.format().equals(mapActionsToDo.get("type"))) {
                restDao.customDbAction(IOParams, mapActionsToDo, config.getKnownTypes());
            } else if (SUPPORTED_ACTION.TRANSFORM.format().equals(mapActionsToDo.get("type"))) {
                // TODO update data
            }
        }
    }

    private String prepareResponseAsJson(JsonObject jsonObject, Map<String, Object> params) {
        final JsonObject json = jsonObject.getJsonObject(SUPPORTED_OUTPUT_FORMAT.JSON.format()).copy();
        for (Map.Entry<String, Object> entryModelJson : json) {
            if (JsonObject.class.isAssignableFrom(entryModelJson.getValue().getClass())) {
                prepareResponseAsJson((JsonObject) entryModelJson.getValue(), params);
            } else if (entryModelJson.getValue().getClass().isAssignableFrom(JsonArray.class)) {
                // prepareResponseAsJson((JsonObject) entry.getValue(), params);
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

    private String prepareResponseAsString(JsonObject jsonObject, Map<String, Object> params) {
        String txtOutputValue = jsonObject.getString(SUPPORTED_OUTPUT_FORMAT.TXT.format());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            final Object o = entry.getValue();
            final String value;
            if (Map.class.isAssignableFrom(o.getClass())) {
                value = new JsonObject((Map) o).toString();
            } else {
                value = o.toString();
            }
            txtOutputValue = txtOutputValue.replaceAll(VARIABLE_PREFIX + entry.getKey(), value);
        }
        return txtOutputValue;
    }

}
