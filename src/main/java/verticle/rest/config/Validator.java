package verticle.rest.config;

public interface Validator<T> {
    boolean isValid(T value);
    boolean isElligible(Object o);
    String getReadableRules();
    T convertObject(Object o);
}
