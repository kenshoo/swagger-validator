package com.kenshoo.swagger.validator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
*/
@SinglePropValidator("type")
public class TypePropertyValidator extends InnerPropertyValidator {

    private final List<FieldTypeValidator> validators;

    public TypePropertyValidator(PropertyValidator parent, Object type) {
        super(parent);
        this.validators =
                Arrays.asList(
                        new ArrayValidator(parent, (String) type),
                        new ForbiddenClassValidator(parent),
                        new PrimitiveValidator(parent),
                        new UnrecommendedClassValidator(parent),
                        new EnumFiledTypeValidator(parent));
    }

    @Override
    public void validate() {
        if (!parent.getPropType().equals(String.class)) {
            // check only the cases when field's type is not String
            // otherwise assume that everything can be converted from String at application level
            boolean oneValidate = false;
            for (FieldTypeValidator validator : validators) {
                if (validator.validates(parent.getPropType())) {
                    validator.validate();
                    oneValidate = true;
                }
            }
            if (!oneValidate) {
                handleWarning("Unvalidated property. Should String be used? Or may be the tool needs an improvement!");
            }
        }
    }

    public static class EnumFiledTypeValidator extends FieldTypeValidator {

        protected EnumFiledTypeValidator(PropertyValidator parent) {
            super(parent);
        }

        @Override
        public boolean validates(Class<?> fieldType) {
            return fieldType.isEnum();
        }

        @Override
        public void validate() {
            handleError("Is enum. Declare it using 'enum' instead of type, or use String.");
        }
    }

    public static class UnrecommendedClassValidator extends FieldTypeValidator {

        protected UnrecommendedClassValidator(PropertyValidator parent) {
            super(parent);
        }

        @Override
        public boolean validates(Class<?> fieldType) {
            return parent.getConf().getUnrecommendedClasses().contains(fieldType);
        }

        @Override
        public void validate() {
            handleWarning("Unrecommended type: {0}. Prefer using String.", parent.getPropType().getName());
        }
    }

    public static class ArrayValidator extends FieldTypeValidator {

        private final String expectedType;

        protected ArrayValidator(PropertyValidator parent, String expectedType) {
            super(parent);
            this.expectedType = expectedType;
        }

        @Override
        public boolean validates(Class<?> fieldType) {
            return fieldType.isArray() || List.class.isAssignableFrom(fieldType);
        }

        @Override
        public void validate() {
            if (!expectedType.equals("array")) {
                handleError("Is an array. Does not match {1}", expectedType);
            }
            Map<String, Object> items = (Map<String, Object>) parent.getYamlProperty().get("items");
            if (items == null) {
                handleError("Items is required for 'array'");
            }
        }
    }

    public static class ForbiddenClassValidator extends FieldTypeValidator {

        protected ForbiddenClassValidator(PropertyValidator parent) {
            super(parent);
        }

        @Override
        public boolean validates(Class<?> fieldType) {
            return parent.getConf().getForbiddenClasses().contains(fieldType);
        }

        @Override
        public void validate() {
            handleError("Type {0} is forbidden to use. Use String instead.", parent.getPropType().getName());
        }
    }

    public static class PrimitiveValidator extends FieldTypeValidator {

        protected PrimitiveValidator(PropertyValidator parent) {
            super(parent);
        }

        @Override
        public boolean validates(Class<?> fieldType) {
            return fieldType.isPrimitive();
        }

        @Override
        public void validate() {
            handleError("Is a primitive. Only objects must be used.");
        }
    }

    public abstract static class FieldTypeValidator extends InnerPropertyValidator implements Validator {

        protected FieldTypeValidator(PropertyValidator parent) {
            super(parent);
        }

        public abstract boolean validates(Class<?> fieldType);
    }
}
