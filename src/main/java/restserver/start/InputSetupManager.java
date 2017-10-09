package restserver.start;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public final class InputSetupManager<T> {

    private final Collection<Parameter<T>> parameters;

    public InputSetupManager(Parameter<T>... p) {
        this.parameters = Collections.unmodifiableCollection(Arrays.asList(p));// quid doublons
    }

    public void setup(T option) {
        for (Parameter<T> parameter : parameters) {
            parameter.execute(option);
        }
    }

    public Collection<Parameter<T>> getParameters() {
        return parameters;
    }
}
