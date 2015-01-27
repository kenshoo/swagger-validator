package com.kenshoo.swagger.validator;

import org.junit.Test;

/**
 */
public class SwaggerValidatorTest {


    @Test
    public void testValidYaml() throws Exception {
        SwaggerValidator swaggerValidator = new SwaggerValidator(getClass().getResourceAsStream("/valid.yaml"));
        swaggerValidator.validateDefinitions();
        swaggerValidator.validateResources();
    }

    @Test(expected = ValidationException.class)
    public void testInvalidPathYaml() throws Exception {
        SwaggerValidator swaggerValidator = new SwaggerValidator(getClass().getResourceAsStream("/invalid_path.yaml"));
        swaggerValidator.validateResources();
    }

    @Test(expected = ValidationException.class)
    public void testMissingOperationYaml() throws Exception {
        SwaggerValidator swaggerValidator = new SwaggerValidator(getClass().getResourceAsStream("/missing_operation.yaml"));
        swaggerValidator.validateResources();
    }

    @Test(expected = ValidationException.class)
    public void testForbiddenTypeYaml() throws Exception {
        SwaggerValidator swaggerValidator = new SwaggerValidator(getClass().getResourceAsStream("/forbidden_type.yaml"));
        swaggerValidator.validateDefinitions();
    }

    @Test(expected = ValidationException.class)
    public void testXJavaClassPresentDefinition() throws Exception {
        new SwaggerValidator(getClass().getResourceAsStream("/no_xjava_definition.yaml")).validateDefinitions();
    }

    @Test(expected = ValidationException.class)
    public void testXJavaClassPresentResource() throws Exception {
        new SwaggerValidator(getClass().getResourceAsStream("/no_xjava_definition.yaml")).validateResources();
    }


}
