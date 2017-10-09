package verticle.rest;

import io.vertx.core.json.JsonArray;
import org.apache.commons.lang3.StringUtils;
import restserver.db.DbAccessor;
import restserver.db.Request;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

public final class RestDao {

    private final static Logger LOGGER = Logger.getLogger(RestService.class.getName());

    private Map<String, DbAccessor> dbAccessorMap;

    public RestDao(Map<String, DbAccessor> dbAccessorMap) {
        this.dbAccessorMap = dbAccessorMap;
    }

    public void customDbAction(Map<String, Object> ioParams, Map<String, Object> mapActionsToDo, Map<String, String> knownTypes) throws SQLException {
        final String dataSourceName = (String) mapActionsToDo.get("in");
        final DbAccessor dbAccessor = dbAccessorMap.get(dataSourceName);
        final String sqlRequestPattern = (String) mapActionsToDo.get("command");
        if (sqlRequestPattern.startsWith("select")) {
            // TODO <should not be executed each request>
            final List<String> inputParameterNames = (List<String>) mapActionsToDo.get("param");
            final List inputParameters = new ArrayList();
            final LinkedHashMap<String, Object> parameters = new LinkedHashMap<>();
            final Map<String, Class> expectedParameters = new HashMap<>();

            if (inputParameterNames != null) {
                for (String parameterName : inputParameterNames) {
                    final Object o = ioParams.get(parameterName);
                    inputParameters.add(o);
                    parameters.put(parameterName, o);
                    final String strType = knownTypes.get(parameterName);
                    Class type = String.class;
                    if (Integer.class.getSimpleName().equals(strType)) {
                        type = Integer.class;
                    }
                    expectedParameters.put(parameterName, type);
                }
            }

            final Map<String, String> expectedSqlOutput = (Map<String, String>) mapActionsToDo.get("output");
            final Request request = new Request(sqlRequestPattern, expectedParameters, expectedSqlOutput);
            // </should not be executed each request>
            final JsonArray read = dbAccessor.read(dbAccessor.connect(), request, parameters);
            final String save = (String) mapActionsToDo.get("save");
            ioParams.put(save, read);
        } else if (sqlRequestPattern.startsWith("insert") || sqlRequestPattern.startsWith("delete")) {
            final List<String> sqlInputParameterNames = (List<String>) mapActionsToDo.get("param");
            // TODO keep key and values in a map instead : key will be used to match  with the knownTypes
            final Map<String, Object> sqlInputParameters = new LinkedHashMap<>();
            for (String key : sqlInputParameterNames) {
                sqlInputParameters.put(key, ioParams.get(key));
            }
            final String keyWhereToSaveTheValue = (String) mapActionsToDo.get("save");
            if (StringUtils.isBlank(keyWhereToSaveTheValue)) {
                dbAccessor.writeAndGetNumberUpdated(dbAccessor.connect(), sqlRequestPattern, sqlInputParameters, knownTypes);
            } else {
                int id = dbAccessor.writeAndGetGeneratedKey(dbAccessor.connect(), sqlRequestPattern, sqlInputParameters, knownTypes);
                ioParams.put(keyWhereToSaveTheValue, id);
            }
        }
    }

}
