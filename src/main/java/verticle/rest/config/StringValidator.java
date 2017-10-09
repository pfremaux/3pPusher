package verticle.rest.config;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public final class StringValidator implements Validator<String> {

    private final Collection<String> allowedValues;
    private final Collection<Pattern> allowedRegex;

    private StringValidator(Collection<String> allowedValues, Collection<Pattern> allowedRegex) {
        this.allowedValues = allowedValues;
        this.allowedRegex = allowedRegex;
    }

    public static StringValidator getInstance(String expression) {
        Set<String> allowedValues = new HashSet<>();
        Set<Pattern> allowedRegex = new HashSet<>();
        expression.startsWith("[");
        expression.endsWith("]");
        String substring = expression.substring(1, expression.length() - 1);
        String[] allowedValuesOrRegex = substring.split(";");
        for (String allowedValueOrRegex : allowedValuesOrRegex) {
            if (allowedValueOrRegex.startsWith("/") && allowedValueOrRegex.endsWith("/")) {
                final String regex = allowedValueOrRegex.substring(1, allowedValueOrRegex.length() - 1);
                final Pattern compile = Pattern.compile(regex);
                allowedRegex.add(compile);
            } else {
                allowedValues.add(allowedValueOrRegex);
            }
        }
        return new StringValidator(allowedValues, allowedRegex);
    }

    @Override
    public boolean isValid(String value) {
        return allowedValues.contains(value) || allowedRegex.stream().anyMatch(r -> r.matcher(value).matches());
    }

    @Override
    public boolean isElligible(Object o) {
        return o instanceof String;
    }

    @Override
    public String getReadableRules() {
        return "allowedValues = " + allowedValues + " ; allowedRegex=" + allowedRegex;
    }

    @Override
    public String convertObject(Object o) {
        return o.toString();
    }
}
