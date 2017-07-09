package com.kenshoo.swagger.validator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.Collections;
import java.util.List;

@Path("/test")
public class SimpleResource {

    @GET
    public List<SimpleModel> getAllSomethings() {
        return Collections.singletonList(new SimpleModel());
    }

    @GET
    @Path("/{id}")
    public SimpleModel getSomething(@PathParam("id") String id) {
        return new SimpleModel();
    }

    @POST
    public SimpleModel postSomething(SimpleModel post) {
        return post;
    }


}
