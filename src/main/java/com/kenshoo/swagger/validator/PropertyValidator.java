package com.kenshoo.swagger.validator;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.Map;


class PropertyValidator implements Validator {

    private final DefinitionValidator definitionValidator;
    private final String propName;
    private final Map<String, Object> property;
    private final Class<?> propType;

    public PropertyValidator(DefinitionValidator definitionValidator, String propName, Map<String, Object> property, PropertyDescriptor propertyDescriptor) {
        this.definitionValidator = definitionValidator;
        this.propName = propName;
        this.property = property;
        this.propType = propertyDescriptor.getType(propName);
    }

    public void validate() {
        if (propType == null) {
            definitionValidator.handleError("Property {0} does not exist.", propName);
        }
        Validator validator = null;
        for (Class<? extends Validator> cls : definitionValidator.getConf().getMutualExclusionPropertyValidators()) {
            SinglePropValidator spvAnnotation = cls.getAnnotation(SinglePropValidator.class);
            Object val = property.get(spvAnnotation.value());
            if (val != null) {
                if (validator != null) {
                    definitionValidator.handleError("Property {0} is not defined well. Define either $ref, or type, or enum.", propName);
                }
                try {
                    Constructor<? extends Validator> constructor = cls.getDeclaredConstructor(PropertyValidator.class, Object.class);
                    constructor.setAccessible(true);
                    validator = constructor.newInstance(this, val);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        if (validator != null) {
            validator.validate();
        } else {
            definitionValidator.handleError("Property {0} is not defined well. Define either $ref, or type, or enum.", propName);
        }
    }

    public void handleError(String msg, Object... arguments) {
        definitionValidator.handleError(preformatMessage(msg), arguments);
    }

    public String handleWarning(String msg, Object... arguments) {
        return definitionValidator.handleWarning(preformatMessage(msg), arguments);
    }

    private String preformatMessage(String msg) {
        return MessageFormat.format("Property {0}: {1}", propName, msg);
    }

    public void addDefinitionToValidate(String def) {
        definitionValidator.addDefinitionToValidate(def);
    }

    public Class<?> getPropType() {
        return propType;
    }

    public Map<String, Object> getYamlProperty() {
        return property;
    }

    public SwaggerValidatorConf getConf() {
        return this.definitionValidator.getConf();
    }

}
