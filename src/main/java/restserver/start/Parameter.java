package restserver.start;

public interface Parameter<T> {

    String description();

    String name();

    void execute(T options);
}
