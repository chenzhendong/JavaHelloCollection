package com.popyoyo.hello.resteasy;

import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Zhendong Chen on 2/14/17.
 */

@Path("/hello")
public class ResteasyService {

    Logger logger = LoggerFactory.getLogger(ResteasyService.class);
    final static Map<Integer, JsonObject> resultMap = new HashMap<>();

    @GET
    @Path("{name}")
    @Produces("application/json")
    public void sayHello(@Suspended final AsyncResponse response, @PathParam("name") String name) throws Exception {
        EventBus bus = VertxMain.vertx.eventBus();
        Response httpRes;

        logger.info("Send to another port ...");

        JsonObject oname = new JsonObject().put("name", name).put("id", 1);

        bus.send("vertx-worker", oname, res -> {
            if (res.succeeded()) {
                logger.info("Recieve from verticle ...");
                JsonObject oresult = (JsonObject)res.result().body();
                logger.info("putting object to hash map, " + oresult);
                resultMap.put(oresult.getInteger("id"), oresult);
                //response.resume(Response.ok(res.result().body()).type(MediaType.TEXT_PLAIN).build());
            } else {
                JsonObject oerror = new JsonObject().put("id", 1).put("error", res.cause().getMessage());
                logger.error(oerror);
                //response.resume(Response.status(500).build());
            }

            //response.resume(httpRes);
        });

        response.resume(Response.accepted().header("Location", "/hello/result/" + 1).build());
    }

    @GET
    @Path("/result/{id}")
    @Produces("application/json")
    public void getResult(@Suspended final AsyncResponse response, @PathParam("id") int id) throws Exception {

        if(resultMap.containsKey(id)) {
            response.resume(Response.status(200).type("application/json").entity(resultMap.get(id).toString()).build());
        } else {
            response.resume(Response.status(404).build());
        }

    }
}

