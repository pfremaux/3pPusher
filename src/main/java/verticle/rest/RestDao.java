package verticle.rest;

import io.vertx.core.json.JsonArray;
import org.apache.commons.lang3.StringUtils;
import thirdpartypusher.db.DbAccessor;
import thirdpartypusher.db.Request;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public class RestDao {

    private final static Logger LOGGER = Logger.getLogger(RestService.class.getName());

    Map<String, DbAccessor> dbAccessorMap;

    public RestDao(Map<String, DbAccessor> dbAccessorMap) {
        this.dbAccessorMap = dbAccessorMap;
    }

    public void customDbAction(Map<String, Object> IOparams, Map<String, Object> mapActionsToDo, Map<String, String> knownTypes) throws SQLException {
        // Getting datasource alias
        String dataSource = (String) mapActionsToDo.get("in");
        DbAccessor dbAccessor = dbAccessorMap.get(dataSource);
        // Getting SQL Request
        String req = (String) mapActionsToDo.get("command");
        if (req.startsWith("select")) {
            List<String> inputParameterNames = (List<String>) mapActionsToDo.get("param");
            List inputParameters = new ArrayList();
            LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            Map<String, Class> expectedParameters = new HashMap<>();
            if (inputParameterNames != null) {
                for (String parameterName : inputParameterNames) {
                    Object o = IOparams.get(parameterName);
                    inputParameters.add(o);
                    parameters.put(parameterName, o);
                    String strType = knownTypes.get(parameterName);
                    Class type = String.class;
                    if (Integer.class.getSimpleName().equals(strType)) {
                        type = Integer.class;
                    }
                    expectedParameters.put(parameterName, type);
                }
            }
            // TODO <should not be executed each request>
            Map<String, String> resultKeys = new HashMap<>();
            Map<String, String> output = (Map<String, String>) mapActionsToDo.get("output");
            // TODO builder une fois pour toute au moment du parsing et non a chaque request
            // TODO on ne serait pas en train de recopier lq mme chose sans utilité ???
            for (Map.Entry<String, String> keyValue : output.entrySet()) {
                resultKeys.put(keyValue.getKey(), keyValue.getValue());
            }
            Request request = new Request(req, expectedParameters, resultKeys);
            // </should not be executed each request>
            JsonArray read = dbAccessor.read(dbAccessor.connect(), request, parameters);
            String save = (String) mapActionsToDo.get("save");
            IOparams.put(save, read);
        } else if (req.startsWith("insert")) {
            // params
            List<String> lst = (List<String>) mapActionsToDo.get("param");
            // TODO keep key and values in a map instead : key will be used to match  with the knownTypes
            List reqParam = new ArrayList<>();
            for (String key : lst) {
                reqParam.add(IOparams.get(key));
            }
            String keyWhereToSaveTheValue = (String) mapActionsToDo.get("save");
            if (StringUtils.isBlank(keyWhereToSaveTheValue)) {

                dbAccessor.writeAndGetNumberUpdated(dbAccessor.connect(), req, reqParam, knownTypes);
            } else {
                int id = dbAccessor.writeAndGetGeneratedKey(dbAccessor.connect(), req, reqParam);
                IOparams.put(keyWhereToSaveTheValue, id);
            }
        }
    }

}
