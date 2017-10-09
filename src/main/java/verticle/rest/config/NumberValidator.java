package verticle.rest.config;

import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class NumberValidator<T extends Number & Comparable> implements Validator<T> {

    private final Collection<T> allowedValues;
    private final Collection<Range<T>> allowedRanges;
    private final Function<String, T> converter;

    protected NumberValidator(Collection<T> allowedValues, Collection<Range<T>> allowedRanges, Function<String, T> converter) {
        this.allowedValues = allowedValues;
        this.allowedRanges = allowedRanges;
        this.converter = converter;
    }

    public static <U extends Number & Comparable<U>> NumberValidator<? extends Number> getNumberValidatorInstance(String expression, Function<String, U> converter) {
        Set<U> allowedValues = new HashSet<>();
        Set<Range<U>> allowedRanges = new HashSet<>();
        expression.startsWith("[");
        expression.endsWith("]");
        String substring = expression.substring(1, expression.length() - 1);
        String[] allowedValuesOrRanges = substring.split(";");
        for (String allowedValueOrRange : allowedValuesOrRanges) {
            final String[] perhapsRange = allowedValueOrRange.split("-");
            if (perhapsRange.length > 1) {
                final U start = converter.apply(perhapsRange[0]);
                final U end = converter.apply(perhapsRange[1]);
                final Range<U> tRange = Range.between(start, end); // TODO perhaps Range is not necessary. It want us to be Comparable...
                allowedRanges.add(tRange);
            } else {
                final String allowedValue = perhapsRange[0];
                allowedValues.add(converter.apply(allowedValue));
            }
        }
        return new NumberValidator<>(allowedValues, allowedRanges, converter);
    }

    @Override
    public boolean isValid(T value) {
        return allowedValues.contains(value) || allowedRanges.stream().anyMatch(r -> r.contains(value));
    }

    @Override
    public boolean isElligible(Object o) {
        return NumberUtils.isNumber(o.toString());
    }

    @Override
    public String getReadableRules() {
        return "allowedRanges = " + allowedRanges + " ; allowedValues = " + allowedValues;
    }

    @Override
    public T convertObject(Object o) {
        return converter.apply(o.toString());
    }
}
