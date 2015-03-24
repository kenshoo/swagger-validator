package com.kenshoo.swagger.validator;

import com.google.common.collect.ImmutableSet;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;


class ResourceValidator implements Validator {

    private final String path;
    private final Map<String, Object> resource;
    private final Map<String, Object> genericMetadata;
    public final static Set<String> forbiddenOperations = ImmutableSet.<String>of("options", "head");
    private final MessageEventHandler messageEventHandler;

    /**
     * @param path the resources path
     * @param resource the resource metadata
     * @param genericMetadata "Definitions" area metadata,
     *                        when resources does not have more concrete configuration,
     *                        default configuration defined in "Definitions" is used
     *                        thus, we would like to have default configuration in the
     *                        resource context.
     */
    public ResourceValidator(String path, Map<String, Object> resource, Map<String, Object> genericMetadata) {
        this.path = path;
        this.resource = resource;
        this.genericMetadata = genericMetadata;
        this.messageEventHandler = new MessageEventHandler(path);
    }

    @Override
    public void validate() {
        try {
            Class<?> cls = SwaggerValidator.getClass(resource);
            if (cls == null) {
                messageEventHandler.handleError("{0} is not defined.", SwaggerValidator.JAVA_CLASS_TAG);
            }
            validatePathAnnotation(cls, path);
            new MimeValidator(cls, path, resource, genericMetadata).validate();
            for (Map.Entry<String, Object> entry : resource.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("x-")) {
                    // ignore custom annotations
                    continue;
                }
                if (key.equalsIgnoreCase("parameters")) {
                    // parameters are not checked
                    continue;
                }
                if (forbiddenOperations.contains(key.toLowerCase())) {
                    messageEventHandler.handleWarning("Operation {0} should not be defined. It's provided by the container.", key);
                    continue;
                }
                if (!isOperationAnnotatedMethodExists(cls, key)) {
                    messageEventHandler.handleError("Method annotated with {0} operation not found in class {1}", key, cls);
                } else {
                    // operation exists, check that it has tags
                    Map<String, Object> operation = (Map<String, Object>) entry.getValue();
                    if (!operation.containsKey("tags")) {
                        messageEventHandler.handleError("Tags must be defined for operation: {0}", key);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            messageEventHandler.handleError("Class not found {0}", e.getMessage());
        }
    }

    private void validatePathAnnotation(Class<?> cls, String path) {
        Path pathAnnotation = cls.getAnnotation(Path.class);
        if (pathAnnotation == null) {
            messageEventHandler.handleError("Path annotation not found on {0}", path, cls);
        }

        if (path.contains(pathAnnotation.value())) {
            String methodPath = path.replace(pathAnnotation.value(), "");
            if (methodPath.length() > 0) {
                for (Method m : cls.getMethods()) {
                    pathAnnotation = m.getAnnotation(Path.class);
                    if (pathAnnotation != null && pathAnnotation.value().equals(methodPath)) {
                        return;
                    }
                }
                messageEventHandler.handleError("No path annotation matches {0}", path);
            }
        } else {
            messageEventHandler.handleError("Path {0} on annotation does not match {1}", pathAnnotation.value(), path);
        }
    }

    /**
     * returns true if at least one public method with the relevant annotation exists
     * @param cls
     * @param operation
     * @return
     */
    private boolean isOperationAnnotatedMethodExists(Class<?> cls, String operation) {
        for (Method m : cls.getMethods()) {
            Annotation[] annotations = m.getDeclaredAnnotations();
            for (Annotation ann : annotations) {
                HttpMethod httpMethod = ann.annotationType().getAnnotation(HttpMethod.class);
                if (httpMethod != null && httpMethod.value().equalsIgnoreCase(operation)) {
                    return true;
                }
            }
        }
        return false;
    }
}
