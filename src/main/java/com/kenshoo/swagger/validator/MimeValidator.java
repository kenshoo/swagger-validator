package com.kenshoo.swagger.validator;

import com.google.common.base.Strings;

import javax.ws.rs.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by michaelp on 3/24/15.
 */
public class MimeValidator implements Validator {

    private final Map<String, Object> resource;
    private final Map<String, Object> genericMetadata;
    private final String path;
    private final MessageEventHandler messageEventHandler;
    private final Class<?> cls;

    public MimeValidator(Class<?> cls, String path, Map<String, Object> resource, Map<String, Object> genericMetadata) {
        this.path = path;
        this.resource = resource;
        this.genericMetadata = genericMetadata;
        this.messageEventHandler = new MessageEventHandler(path);
        this.cls = cls;
    }

    /**
     * Validates that Mime types defined in swagger meta file are valid,
     *
     * In YAML mime definitions can appear in:
     *    1. root "Definitions" area
     *    2. specific path method
     *
     * In Java Class mime definitions can appear in:
     *    1. Resource (i.e java class)
     *    2. Method
     *
     * @param cls class to check
     */
    private void validateMimeAnnotationsValidity(Class<?> cls) {
        Produces producesClassAnnotation = cls.getAnnotation(Produces.class);
        Consumes consumesClassAnnotation = cls.getAnnotation(Consumes.class);
        String producesYamlMime=null, consumesYamlMime=null;
        Object producesMeta, consumesMeta;
        Method method=null;

        validateRootMimes(producesClassAnnotation, consumesClassAnnotation);
        for (Map.Entry<String, Object> entry : resource.entrySet()) {
            if (entry.getKey().equals("x-javaClass")) continue;
            method = getJaxRsMethod(cls, entry.getKey());
            if (method != null) {
                if (entry.getValue() instanceof Map) {
                    Map<String, Object> entryItems = (Map<String, Object>)entry.getValue();
                    producesMeta = entryItems.get("produces");
                    if (producesMeta != null && ((List)producesMeta).size() >=1){
                        producesYamlMime = getMimeType(((List) producesMeta).get(0).toString());
                    }
                    consumesMeta = entryItems.get("consumes");
                    if (consumesMeta != null && ((List)consumesMeta).size() >=1){
                        consumesYamlMime = getMimeType(((List) consumesMeta).get(0).toString());
                    }
                }
                validateMime(cls, consumesClassAnnotation, consumesYamlMime, method, Consumes.class);
                validateMime(cls, producesClassAnnotation, producesYamlMime, method, Produces.class);
            }
        }
    }

    /**
     * Validates jax-rs mime type against documentation in swagger
     * first on method level then (on absence) at class level.
     *
     * @param cls resource class
     * @param classAnnotation mime class annotation
     * @param yamlMime mime type defined in yaml
     * @param method method which annotation can override mime defined on class
     */
    private void validateMime(Class<?> cls, Annotation classAnnotation, String yamlMime,
                              Method method, Class<? extends Annotation> methodAnnoatation) {
        Annotation methodAnnotation = method.getAnnotation(methodAnnoatation);
        String classAnnotationValue = getAnnotationValue(classAnnotation);

        if (methodAnnotation != null) {
            String methodAnnotationValue = getAnnotationValue(methodAnnotation);
            if (yamlMime != null &&
                    !yamlMime.equals(methodAnnotationValue)) {
                messageEventHandler.handleError(
                        "Consumes mime {0} does not match {1} method annotation in {2}, method {3}.",
                        yamlMime,
                        methodAnnotation,
                        cls,
                        method
                );
            }
        } else if (classAnnotation != null) {
            if (!Strings.isNullOrEmpty(yamlMime) && !yamlMime.equals(classAnnotationValue)) {
                messageEventHandler.handleError(
                        "Produces mime {0} does not match {1} class annotation in {2}.",
                        yamlMime,
                        classAnnotation,
                        cls
                );
            }
        }
    }

    /**
     * Validates that root defined mime types are valid
     *
     * @param producesClassAnnotation
     * @param consumesClassAnnotation
     */
    private void validateRootMimes(Produces producesClassAnnotation, Consumes consumesClassAnnotation) {
        validateRootMime("produces", producesClassAnnotation);
        validateRootMime("consumes", consumesClassAnnotation);
    }

    /**
     * Validates specific mime that defined in
     * "Definitions" area of the yaml (i.e root mime)
     *
     * @param key the mime name
     * @param mimeAnnotation actual annotation representing this mime
     */
    private void validateRootMime(String key, Annotation mimeAnnotation) {
        List<String> rootMimes = (List<String>)genericMetadata.get(key);

        if (mimeAnnotation != null) {
            String mimeAnnotationValue = getAnnotationValue(mimeAnnotation);
            if (rootMimes != null && rootMimes.size() >= 1 &&
                    !Strings.isNullOrEmpty(mimeAnnotationValue) &&
                    !mimeAnnotationValue.equalsIgnoreCase(rootMimes.get(0))) {
                messageEventHandler.handleError(
                        "{0} root mime {1} does not match {2} defined in yaml.",
                        key.toUpperCase(),
                        mimeAnnotationValue,
                        rootMimes.get(0)
                );
            }
        }
    }

    /**
     * Extracts annotation value.
     *
     * @param annotation
     * @return
     */
    private String getAnnotationValue(Annotation annotation) {
        if (annotation != null) {
            String value = annotation.toString();
            return value.substring(
                    value.indexOf("[", 0) + 1,
                    value.lastIndexOf("]")
            );
        }
        return null;
    }

    /**
     * Extracts actual mime from doc string
     *
     * @param producesDocumentation
     * @return MimeType
     */
    private String getMimeType(String producesDocumentation) {
        return producesDocumentation != null &&
                producesDocumentation.startsWith("[") && producesDocumentation.endsWith("]") ?
                producesDocumentation.substring(1, producesDocumentation.length()-1)
                :
                producesDocumentation;
    }

    /**
     * Fetches method corresponding to given jax-rs annotation.
     *
     * @param cls class to look at
     * @param httpMethod jax-rs annotation
     *
     * @return Method
     */
    private Method getJaxRsMethod(Class<?> cls, String httpMethod) {
        Class<? extends Annotation> annotationClass = resolveJaxRsAnnotation(httpMethod);
        for (Method m : cls.getMethods()) {
            Object jaxrsMethodAnnotation = m.getAnnotation(annotationClass);
            Object pathAnnotation = m.getAnnotation(Path.class);
            if (jaxrsMethodAnnotation != null && pathAnnotation == null){
                return m;
            }
        }
        return null;
    }

    /**
     * Locates corresponding jax-rs annotation by it's name
     *
     * @param httpMethod method name
     *
     * @return jax-rs annotation
     */
    private Class<? extends Annotation> resolveJaxRsAnnotation(String httpMethod) {
        if(!Strings.isNullOrEmpty(httpMethod)) {
            switch (httpMethod) {
                case "get":
                    return GET.class;
                case "post":
                    return POST.class;
                case "put":
                    return PUT.class;
                case "delete":
                    return DELETE.class;

            }
        }
        return null;
    }

    /**
     * Validates that mime annotation exist either on Class or on Method
     *
     * @param cls
     */
    private void validateMimeAnnotationsExistence(Class<?> cls) {
        Produces producesMethodAnnotation, producesClassAnnotation = cls.getAnnotation(Produces.class);
        Consumes consumesMethodAnnotation, consumesClassAnnotation = cls.getAnnotation(Consumes.class);

        for (Method m : cls.getMethods()) {
            if (isJaxRsMethod(m)) {
                producesMethodAnnotation = m.getAnnotation(Produces.class);
                if (producesClassAnnotation == null && producesMethodAnnotation == null) {
                    messageEventHandler.handleError(
                            "Produces annotation is not found neither " +
                            "on {0} level nor on method {1}.", cls, m
                    );
                }
                consumesMethodAnnotation = m.getAnnotation(Consumes.class);
                if (consumesClassAnnotation == null && consumesMethodAnnotation == null) {
                    messageEventHandler.handleError(
                            "Consumes annotation is not found neither " +
                            "on {0} level nor on method {1}.", cls, m
                    );
                }
            }
        }
    }

    /**
     * Checks whether given method has one of
     * get/post/put/delete jax-rs annotations
     *
     * @param m method to check
     *
     * @return boolean
     */
    private boolean isJaxRsMethod(Method m) {
        return m.getAnnotation(HttpMethod.class) != null;
    }

    /**
     * Validates that:
     *  1. Mime annotations exist either on Class or on Method
     *  2. Mime types defined in swagger meta file is valid
     */
    @Override
    public void validate() {
        validateMimeAnnotationsExistence(cls);
        validateMimeAnnotationsValidity(cls);
    }
}
