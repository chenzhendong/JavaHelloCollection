package com.popyoyo.hello.resteasy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

/**
 * Created by Zhendong Chen on 2/14/17.
 */
public class MessageWorker extends AbstractVerticle {

    public void start() throws Exception{
        EventBus bus = vertx.eventBus();
        bus.consumer("vertx-worker", msg->{
            try {
                Thread.currentThread().sleep(5000);
                JsonObject o = (JsonObject)msg.body();
                o.put("result", "Hello " + o.getString("name") + "!!!");
                msg.reply(o);
            } catch (Exception ex) {
                System.out.println("Error happens ...");
            }
        });

    }
}
