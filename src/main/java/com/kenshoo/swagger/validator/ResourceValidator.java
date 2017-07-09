package com.kenshoo.swagger.validator;

import com.google.common.collect.ImmutableSet;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Set;


class ResourceValidator implements Validator {

    private final String path;
    private final Map<String, Object> resource;
    public final static Set<String> forbiddenOperations = ImmutableSet.<String>of("options", "head");

    public ResourceValidator(String path, Map<String, Object> resource) {
        this.path = path;
        this.resource = resource;
    }

    @Override
    public void validate() {
        try {
            Class<?> cls = SwaggerValidator.getClass(resource);
            if (cls == null) {
                handleError("{0} is not defined.", SwaggerValidator.JAVA_CLASS_TAG);
            }

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
                    handleWarning("Operation {0} should not be defined. It's provided by the container.", key);
                    continue;
                }
                if (!isOperationAnnotatedMethodExists(cls, path, key)) {
                    handleError("Method annotated with {0} operation not found in class {1}", key, cls);
                } else {
                    // operation exists, check that it has tags
                    Map<String, Object> operation = (Map<String, Object>) entry.getValue();
                    if (!operation.containsKey("tags")) {
                        handleError("Tags must be defined for operation: {0}", key);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            handleError("Class not found {0}", e.getMessage());
        }
    }

    /**
     * returns true if at least one public method with the relevant annotation exists
     * @param cls
     * @param path
     * @param operation
     * @return
     */
    private boolean isOperationAnnotatedMethodExists(Class<?> cls, String path, String operation) {
        Path pathAnnotation = cls.getAnnotation(Path.class);

        if (pathAnnotation == null) {
            handleError("Path annotation not found on {0}", cls);
        } else if (!path.contains(pathAnnotation.value())) {
            handleError("Path {0} on annotation does not match {1}", pathAnnotation.value(), path);
        }

        String methodPath = path.replace(pathAnnotation.value(), "");

        for (Method m : cls.getMethods()) {
            Path methodPathAnnotation = m.getAnnotation(Path.class);
            if ((methodPath.length() == 0 && methodPathAnnotation == null) || (methodPathAnnotation != null && methodPath.equals(methodPathAnnotation.value()))) {
                Annotation[] annotations = m.getDeclaredAnnotations();
                for (Annotation ann : annotations) {
                    HttpMethod httpMethod = ann.annotationType().getAnnotation(HttpMethod.class);
                    if (httpMethod != null && httpMethod.value().equalsIgnoreCase(operation)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void handleError(String msg, Object... arguments) {
        String formatted = handleWarning(msg, arguments);
        throw new ValidationException(formatted);
    }

    public String handleWarning(String msg, Object... arguments) {
        String formatted = MessageFormat.format("Path: {0}: {1}", path, MessageFormat.format(msg, arguments));
        System.out.println(formatted);
        return formatted;
    }
}
