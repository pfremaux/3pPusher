package verticle;

import java.util.*;

public class ConfigCustom {

    private Integer port = 8080;
    private String method = "GET";
    private String path = "/";
    private Map<String, String> urlParam = Collections.emptyMap();
    private Map<String, String> query = Collections.emptyMap();
    private HashMap<String, Object> response = new HashMap<>();
    private List<Map<String, Object>> actions = new ArrayList<>();

    @Override
    public String toString() {
        return "ConfigCustom{" +
                "port=" + port +
                ", path='" + path + '\'' +
                ", query=" + query +
                ", response=" + response +
                '}';
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
}
