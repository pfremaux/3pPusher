package verticle.rest.config;

import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NumberValidatorTest {

    @Test
    public void testKaRienAVoir() throws Exception {
        //String value = "Integer[0-5;8]";
        String value = "Integer";
        final Pattern firstPartInputType = Pattern.compile("[a-zA-Z]*");
        final Matcher matcher = firstPartInputType.matcher(value);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }


    @Test
    public void simpleNumberValid() throws Exception {
        NumberValidator validator = NumberValidator.getNumberValidatorInstance("[5]", Integer::parseInt);
        junit.framework.Assert.assertTrue("Should be valid", validator.isValid(5));
    }

    @Test
    public void rangeNumberValid() throws Exception {
        NumberValidator validator = NumberValidator.getNumberValidatorInstance("[5-7]", Integer::parseInt);
        junit.framework.Assert.assertTrue("Should not be valid", !validator.isValid(4));
        junit.framework.Assert.assertTrue("Should be valid", validator.isValid(5));
        junit.framework.Assert.assertTrue("Should be valid", validator.isValid(6));
        junit.framework.Assert.assertTrue("Should be valid", validator.isValid(7));
        junit.framework.Assert.assertTrue("Should not be valid", !validator.isValid(8));
    }

    @Test
    public void simpleAndRangeNumberValid() throws Exception {
        NumberValidator validator = NumberValidator.getNumberValidatorInstance("[2;5-7]", Integer::parseInt);
        junit.framework.Assert.assertTrue("Should be valid", validator.isValid(2));
        junit.framework.Assert.assertTrue("Should not be valid", !validator.isValid(4));
        junit.framework.Assert.assertTrue("Should be valid", validator.isValid(5));
        junit.framework.Assert.assertTrue("Should be valid", validator.isValid(6));
        junit.framework.Assert.assertTrue("Should be valid", validator.isValid(7));
        junit.framework.Assert.assertTrue("Should not be valid", !validator.isValid(8));
    }

}