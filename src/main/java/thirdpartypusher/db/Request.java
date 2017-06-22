package thirdpartypusher.db;


import java.util.Map;

public class Request {

    private final String request;
    private final Map<String, Class> expectedParameters;
    private final Map<String, String> expectedResultPerRow;

    public Request(String request, Map<String, Class> expectedParameters, Map<String, String> expectedResultPerRow) {
        this.request = request;
        this.expectedParameters = expectedParameters;
        this.expectedResultPerRow = expectedResultPerRow;
    }

    public String getRequest() {
        return request;
    }

    public Map<String, Class> getExpectedParameters() {
        return expectedParameters;
    }

    public Map<String, String> getExpectedResultPerRow() {
        return expectedResultPerRow;
    }
}
