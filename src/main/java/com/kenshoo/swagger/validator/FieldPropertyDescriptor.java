package com.kenshoo.swagger.validator;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;


public class FieldPropertyDescriptor implements PropertyDescriptor{

    private final Class<?> cls;

    public FieldPropertyDescriptor(Class<?> cls) {
        this.cls = cls;
    }

    @Override
    public Class<?> getType(String propName) {
        Field field = ReflectionUtils.findField(cls, propName);
        if (field != null) {
            return field.getType();
        }
        return null;
    }
}
