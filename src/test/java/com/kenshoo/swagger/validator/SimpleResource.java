package com.kenshoo.swagger.validator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/test")
public class SimpleResource {

    @GET
    public SimpleModel getSomething() {
        return new SimpleModel();
    }

    @POST
    public SimpleModel postSomething(SimpleModel post) {
        return post;
    }



}
