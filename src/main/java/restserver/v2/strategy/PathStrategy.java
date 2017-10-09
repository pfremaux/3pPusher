package restserver.v2.strategy;

import java.util.Map;

public class PathStrategy implements ActionStrategy {

    @Override
    public String getNode() {
        return NodeKey.PATH.key;
    }

    @Override
    public void init() {

    }

    @Override
    public void validate(Map<String, Object> yamlConfig, String parentNode) {
        if (parentNode != null) {
            // todo must be at root level
        }
    }



    @Override
    public void execute(Map<String, Object> config, Map<String, Object> values) {

    }
}
