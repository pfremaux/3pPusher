package verticle.rest.config;

import java.util.*;
import java.util.regex.Pattern;

public class ConfigCustom {

    public static final List<String> SUPPORTED_HTTP_METHOD = Arrays.asList("GET", "POST", "PUT", "DELETE");
    private static Pattern pathPattern = Pattern.compile("[/a-z0-9:]+"); // TODO améliorer
    private static Pattern pathParameterPattern = Pattern.compile("\\:[a-z0-9]+");

    private String host = "localhost";
    private Integer port = 8080;
    private String method = "GET";
    private String path = "/";
    private Map<String, String> urlParam = Collections.emptyMap();
    private Map<String, String> body = Collections.emptyMap();
    private Map<String, String> query = Collections.emptyMap();
    private HashMap<String, Object> response = new HashMap<>();
    private List<Map<String, Object>> actions = new ArrayList<>();
    private Map<String, String> knownTypes = new HashMap<>();

    public String toSimpleString() {
        return method + " " + host + ":" + port + path;
    }

    @Override
    public String toString() {
        return "ConfigCustom{" +
                "port=" + port +
                ", path='" + path + '\'' +
                ", query=" + query +
                ", response=" + response +
                '}';
    }

    public void validate() {
        if (!pathPattern.matcher(path).matches()) {
            throw new RuntimeException(path + " is not a valid relative path.");
        }
        Set<String> allParameters = new HashSet<>();
        Set<String> inputInUrl = new HashSet<>();
        for (String s : path.split("/")) {
            if (s.startsWith(":")) {
                String var = s.substring(1);
                inputInUrl.add(var);
            }
        }
        int nbUrlParam = urlParam.size();
        if (inputInUrl.size() > nbUrlParam) {
            for (String input : inputInUrl) {
                if (!urlParam.containsKey(input)) {
                    throw new RuntimeException(input + " is not defined in urlParam Key.");
                }
            }
        } else if (inputInUrl.size() < nbUrlParam) {
            for (String input : urlParam.keySet()) {
                if (!inputInUrl.contains(input)) {
                    throw new RuntimeException(input + " is defined in urlParam Key but is no in the path input.");
                }
            }
        }
        allParameters.addAll(inputInUrl);
        allParameters.addAll(query.keySet());
        allParameters.addAll(body.keySet());
        knownTypes.putAll(body);

        if (!SUPPORTED_HTTP_METHOD.contains(method)) {
            throw new RuntimeException(method + " is not a valid HTTP method.");
        }

        for (Map<String, Object> action : actions) {
            final Object param = action.get("param");
            if (param != null) {
                Collection<String> listParam = (Collection<String>) param;
                for (String aParameterName : listParam) {
                    if (!allParameters.contains(aParameterName)) {
                        throw new RuntimeException(aParameterName + " does not exist. It can't be used as a input parameter for " + this.toSimpleString());
                    }
                }
            }
            Object type = action.get("type");
            if (type != null && type.toString().equalsIgnoreCase("sql")) {
                Object command = action.get("command");
                if (command == null) {
                    throw new RuntimeException("Command key can't be null for action type 'sql'.");
                }
                String strCommand = (String) command;
                if (strCommand.startsWith("select")) {
                    String columns = strCommand.substring("select".length(), strCommand.indexOf("from")).trim();
                    List<String> strings = Arrays.asList(columns.replaceAll(" ", "").split(","));
                    // TODO output may be null ?
                    Map<String, String> output = (Map<String, String>) action.get("output");
                    knownTypes.putAll(output);
                    for (String key : output.keySet()) {
                        if (!strings.contains(key)) {
                            throw new RuntimeException(key + " not found as output in query." + strCommand);
                        }
                    }
                    // TODO builder une fois pour toute ici par exemple ou en dehors des if else
                } else if (strCommand.startsWith("insert")) {

                } else if (strCommand.startsWith("delete")) {

                } else if (strCommand.startsWith("update")) {

                } else {
                    throw new RuntimeException("Unrecognized SQL command : " + strCommand);
                }
            }
        }
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Map<String, String> getQuery() {
        return query;
    }

    public void setQuery(Map<String, String> query) {
        this.query = query;
    }

    public Map<String, Object> getResponse() {
        return (Map<String, Object>) Collections.unmodifiableMap(response);
    }

    public void setResponse(HashMap<String, Object> response) {
        this.response = response;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Map<String, String> getUrlParam() {
        return urlParam;
    }

    public void setUrlParam(Map<String, String> urlParam) {
        this.urlParam = urlParam;
    }

    public List<Map<String, Object>> getActions() {
        return actions;
    }

    public void setActions(List<Map<String, Object>> actions) {
        this.actions = actions;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public void setBody(Map<String, String> body) {
        this.body = body;
    }


    public Map<String, String> getKnownTypes() {
        return knownTypes;
    }

    public void setKnownTypes(Map<String, String> knownTypes) {
        this.knownTypes = knownTypes;
    }
}
