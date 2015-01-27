package com.kenshoo.swagger.validator;


public interface PropertyDescriptor {

    /**
     * Returns type of the property or null if property not found
     */
    Class<?> getType(String propName);

}
