package com.kenshoo.swagger.validator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
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
