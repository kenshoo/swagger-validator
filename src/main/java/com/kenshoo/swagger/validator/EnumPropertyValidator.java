package com.kenshoo.swagger.validator;

import java.util.List;

@SinglePropValidator("enum")
public class EnumPropertyValidator extends InnerPropertyValidator {
    private final List<String> validValues;

    public EnumPropertyValidator(PropertyValidator parent, Object validValues) {
        super(parent);
        this.validValues = (List<String>) validValues;
    }

    @Override
    public void validate() {
        if (validValues == null)
            handleError("Enum is empty!");
        else if (!parent.getPropType().equals(String.class)) {
            handleWarning("It's recommended to use String with enums!");
            if (parent.getPropType().isEnum()) {
                // TODO: validate valid values
            } else {
                handleError("Must be either Enum or String");
            }
        }
    }
}
