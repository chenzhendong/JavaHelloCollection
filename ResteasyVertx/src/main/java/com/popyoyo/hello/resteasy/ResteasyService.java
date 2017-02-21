package com.popyoyo.hello.resteasy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Created by Zhendong Chen on 2/14/17.
 */

@Path("/")
public class ResteasyService extends AbstractVerticle{

    public ResteasyService(){
        VertxMain.vertx.deployVerticle(this);
    }

    @GET
    @Path("{name}")
    @Produces("text/plain")
    public void sayHello(@Suspended final AsyncResponse response, @PathParam("name")String name) throws Exception
    {
        EventBus bus = vertx.eventBus();

        System.out.println("Send to another port ...");

        bus.send("vertx-worker", name, res->{
            Response httpRes;
            if(res.succeeded()) {
                System.out.println("Recieve from verticle ...");
                httpRes = Response.ok(res.result().body()).type(MediaType.TEXT_PLAIN).build();
            } else {
                System.out.println(res.cause().getMessage());
                httpRes = Response.status(500).build();
            }
            response.resume(httpRes);
        });
    }
}

