package com.kenshoo.swagger.validator;

import com.google.common.collect.ImmutableSet;

import java.util.*;

/**
 * Provides a configuration for YamlValidator.
 *
 */
public class SwaggerValidatorConf {


    private final static List<Class<? extends InnerPropertyValidator>> mutualExclusionPropertyValidators =
            Arrays.asList(RefPropertyValidator.class,
                    EnumPropertyValidator.class,
                    TypePropertyValidator.class);

    private static final Set<Class<?>> unrecommendedClasses = ImmutableSet.
            <Class<?>>of(Long.class, Float.class, Integer.class, Boolean.class, Double.class, Byte.class, Character.class);

    private static final Set<Class<?>> forbiddenClasses = ImmutableSet.<Class<?>>of(Date.class, Calendar.class);

    /**
     * The mutual exclusion property validators are elements of a property element that must be mutually exclusive
     * (meaning only one of the elements must be present)
     * By default the list includes 'type', '$ref' and 'enum'.
     *
     * Override this method to customize the list.
     * @return
     */
    public List<Class<? extends InnerPropertyValidator>> getMutualExclusionPropertyValidators() {
        return mutualExclusionPropertyValidators;
    }

    /**
     * Creates a property descriptor for the provided class.
     * Override this method to provide a different PropertyDescriptor.
     */
    public PropertyDescriptor createPropertyDescriptor(Class<?> cls) {
        return new FieldPropertyDescriptor(cls);
    }


    /**
     * Set of classes that are not recommended to be used in definitions. Class in this set causes the validator to print warning.
     */
    public Set<Class<?>> getUnrecommendedClasses() {
        return unrecommendedClasses;
    }

    /**
     * Set of classes that are forbidden to be used in definitions.
     * Detecting such a class on a definition will fail the validation.
     */
    public Set<Class<?>> getForbiddenClasses() {
        return forbiddenClasses;
    }

    /**
     * Creates a default SwaggerValidatorConf
     * @return
     */
    public static SwaggerValidatorConf getDefault() {
        return new SwaggerValidatorConf();
    }


}
