package com.kenshoo.swagger.validator;

import java.text.MessageFormat;
import java.util.Map;


class DefinitionValidator implements Validator {

    private final String name;
    private final Map<String, Object> definition;
    private final SwaggerValidator swaggerValidator;

    public DefinitionValidator(SwaggerValidator swaggerValidator, String name, Map<String, Object> definition) {
        this.swaggerValidator = swaggerValidator;
        this.name = name;
        this.definition = definition;
    }

    public void validate() {
        try {
            Class<?> cls = SwaggerValidator.getClass(definition);
            if (cls == null) {
                handleError("{0} is not defined.", SwaggerValidator.JAVA_CLASS_TAG);
            }
            PropertyDescriptor propertyDescriptor = getConf().createPropertyDescriptor(cls);
            Map<String, Object> properties = (Map<String, Object>) definition.get("properties");
            for (Map.Entry<String, Object> propEntry : properties.entrySet()) {
                new PropertyValidator(this, propEntry.getKey(), (Map<String, Object>) propEntry.getValue(), propertyDescriptor).validate();
            }
        } catch (ClassNotFoundException e) {
            handleError("Class not found {0}", e.getMessage());
        }
    }

    public void addDefinitionToValidate(String definition) {
        swaggerValidator.addDefinitionToValidate(definition);
    }

    public void handleError(String msg, Object... arguments) {
        String formatted = handleWarning(msg, arguments);
        throw new ValidationException(formatted);
    }

    public String handleWarning(String msg, Object... arguments) {
        String formatted = MessageFormat.format("Definition: {0}: {1}", name, MessageFormat.format(msg, arguments));
        System.out.println(formatted);
        return formatted;
    }

    public SwaggerValidatorConf getConf() {
        return swaggerValidator.getConf();
    }
}
