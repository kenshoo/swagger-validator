package com.kenshoo.swagger.validator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/method-mime")
public class SimpleResourceMethodMime {

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @GET
    public SimpleModel getSomething() {
        return new SimpleModel();
    }

    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public SimpleModel postSomething(SimpleModel post) {
        return post;
    }
}
