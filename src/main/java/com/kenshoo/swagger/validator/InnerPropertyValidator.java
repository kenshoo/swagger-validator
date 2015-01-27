package com.kenshoo.swagger.validator;


public abstract class InnerPropertyValidator implements Validator {

    protected final PropertyValidator parent;

    protected InnerPropertyValidator(PropertyValidator parent) {
        this.parent = parent;
    }

    protected void handleError(String msg, Object... arguments) {
        parent.handleError(msg, arguments);
    }

    protected String handleWarning(String msg, Object... arguments) {
        return parent.handleWarning(msg, arguments);
    }

}
