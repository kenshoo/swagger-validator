# Swagger Validator
[![Build Status](https://travis-ci.org/kenshoo/swagger-validator.svg?branch=master)](https://travis-ci.org/kenshoo/swagger-validator)

## Overview
Validates swagger.yaml file.
Validation are done assuming JAX-RS resources and Jackson POJOs.

The default validations include:
- Resources validations:
  - The resource class exists (See x-javaClass)
  - The resource class annotated with @Path and that the value matches.
  - For each operation validate that:
    - Exists method in a resource annotated with the proper annotation (e.g. GET, POST, etc.)
    - Operation is tagged.
- Definitions validations:
  -  The POJO exists (See x-javaClass)
  -  Property from definition matches a property in POJO (By default property in Swagger equals to field in POJO)
  -  POJO doesn't use forbidden types (e.g. using primitive types is forbidden)
  -  Warning is printed if unrecommended type is used.

## Validator Elements
The Swagger Validator expects additional elements to be present in the swagger.yaml to perform the validations.
#### x-javaClass
**x-javaClass** defines the fully qualified name of the desired class. This is used to validate that the relevant class really exists in the classpath and it's a starting point for addition validations.

## Download
The Swagger Validator is distributed using Maven Central.

### Maven Dependency
```
<dependency>
  <groupId>com.kenshoo</groupId>
  <artifactId>swagger-validator</artifactId>
  <version>${swagger-validator-version}</version>
</dependency>
```

## Running 
The SwaggerValidator is a simple Java class. It must be run in the classpath containing all the resources and definitions. 

#### Example
```
SwaggerValidator swaggerValidator = new SwaggerValidator(getClass().getResourceAsStream("/swagger.yaml"));
swaggerValidator.validateDefinitions();
swaggerValidator.validateResources();
```
See unit tests for more examples.

#### Customization
```
SwaggerValidatorConf conf = new SwaggerValidatorConf() {
    @Override
    public Set<Class<?>> getForbiddenClasses() {
        return Collections.emptySet();
    }
};
SwaggerValidator swaggerValidator = new SwaggerValidator(getClass().getResourceAsStream("/swagger.yaml"), conf);
swaggerValidator.validateDefinitions(); // forbidden types won't be validated
```
