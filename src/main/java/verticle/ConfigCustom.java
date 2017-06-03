package verticle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ConfigCustom {

    private Integer port = 8080;
    private String method = "GET";
    private String path = "/";
    private Map<String, String> urlParam = Collections.emptyMap();
    private Map<String, String> query = Collections.emptyMap();
    private Map<String, Object> response = Collections.emptyMap();
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
        return response;
    }

    public void setResponse(Map<String, Object> response) {
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
