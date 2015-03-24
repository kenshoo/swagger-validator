package com.kenshoo.swagger.validator;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path("/class-mime")
public class SimpleResourceNoMime {

    @GET
    public SimpleModel getSomething() {
        return new SimpleModel();
    }

    @POST
    public SimpleModel postSomething(SimpleModel post) {
        return post;
    }
}
