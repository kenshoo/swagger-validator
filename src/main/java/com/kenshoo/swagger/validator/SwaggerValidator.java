package com.kenshoo.swagger.validator;

import org.springframework.util.ClassUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;


/**
 * Validates that swagger.yaml file is according to guidelines.
 *
 * Currently validates only data model (definitions) and resources (paths).
 *
 * In order to customize the behavior, extend the SwaggerValidatorConf and override the relevant methods.
 * (See SwaggerValidatorConf javadoc for more details)
 *
 * Note on synchronization: Class is not synchronized and must be run in a single thread!
 *
 */
public class SwaggerValidator {

    public static final String JAVA_CLASS_TAG = "x-javaClass";
    private final Map<String, Object> yaml;
    private final Set<String> uncheckedDefinitions = new HashSet<>();
    private final Set<String> checkedDefinitions = new HashSet<>();

    public final SwaggerValidatorConf conf;

    public SwaggerValidator(Map<String, Object> yaml, SwaggerValidatorConf conf) {
        this.yaml = yaml;
        this.conf = conf;
    }

    public SwaggerValidator(Map<String, Object> yaml) {
        this(yaml, SwaggerValidatorConf.getDefault());
    }

    /**
     * Loads yaml from input stream.
     */
    public SwaggerValidator(InputStream is, SwaggerValidatorConf conf) throws FileNotFoundException {
        this((Map<String, Object>) new Yaml().load(is), conf);
    }

    /**
     * Loads yaml from input stream and uses default configuration for validation.
     */
    public SwaggerValidator(InputStream is) throws FileNotFoundException {
        this((Map<String, Object>) new Yaml().load(is));
    }

    /**
     * Validates the definitions
     */
    public void validateDefinitions() {
        Map<String, Object> definitions = (Map<String, Object>) yaml.get("definitions");
        for (Map.Entry<String, Object> defEntry : definitions.entrySet()) {
            String name = defEntry.getKey();
            new DefinitionValidator(this, name, (Map<String, Object>) defEntry.getValue()).validate();
            checkedDefinitions.add(name);
            uncheckedDefinitions.remove(name);
        }
        if (!uncheckedDefinitions.isEmpty()) {
            throw new ValidationException("Undefined reference: " + uncheckedDefinitions);
        }
    }

    /**
     * Validates resources
     */
    public void validateResources() {
        Map<String, Object> paths = (Map<String, Object>) yaml.get("paths");
        Map<String, Object> genericMetadata = getGenericMetadata("consumes", "produces");
        for (Map.Entry<String, Object> pathEntry : paths.entrySet()) {
            String path = pathEntry.getKey();
            new ResourceValidator(path,
                    (Map<String, Object>) pathEntry.getValue(),
                    genericMetadata
            ).validate();
        }
    }

    /**
     * Retrieves items from yaml based on specified keys
     *
     * @param items
     *
     * @return Map<String, Object>
     */
    private Map<String, Object> getGenericMetadata(String... items) {
        Map<String, Object> genericMetadata = new HashMap<>();
        for (String item : items) {
            Object data = yaml.get(item);
            if (data != null) {
                genericMetadata.put(item, data);
            }
        }
        return genericMetadata;
    }

    void addDefinitionToValidate(String definition) {
        if (!checkedDefinitions.contains(definition)) {
            uncheckedDefinitions.add(definition);
        }
    }

    static Class<?> getClass(Map<String, Object> struct) throws ClassNotFoundException {
        String definitionJavaClassName = (String) struct.get(SwaggerValidator.JAVA_CLASS_TAG);
        if (definitionJavaClassName == null) {
            return null;
        }
        return ClassUtils.forName(definitionJavaClassName, SwaggerValidator.class.getClassLoader());
    }

    public SwaggerValidatorConf getConf() {
        return conf;
    }
}
