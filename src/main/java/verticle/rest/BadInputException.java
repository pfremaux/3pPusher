package verticle.rest;

import verticle.rest.config.Validator;

public class BadInputException extends Throwable {

    private final String msg;
    private final Validator validator;

    public BadInputException(String msg, Validator validator) {
        this.msg = msg;
        this.validator = validator;
    }

    public String getMsg() {
        return msg;
    }

    public Validator getValidator() {
        return validator;
    }

    @Override
    public String toString() {
        return super.toString() + " ; " + msg + " ; Allowed rules : " + validator.getReadableRules();
    }
}
