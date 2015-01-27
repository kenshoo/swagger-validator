package com.kenshoo.swagger.validator;


@SinglePropValidator("$ref")
public class RefPropertyValidator extends InnerPropertyValidator {

    private final String ref;

    public RefPropertyValidator(PropertyValidator parent, Object ref) {
        super(parent);
        this.ref = (String) ref;
    }

    @Override
    public void validate() {
        parent.addDefinitionToValidate(ref.replace("#/definitions/", ""));
    }
}
