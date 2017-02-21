package com.popyoyo.hello.resteasy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;

/**
 * Created by Zhendong Chen on 2/14/17.
 */
public class MessageWorker extends AbstractVerticle {

    public void start() throws Exception{
        EventBus bus = vertx.eventBus();
        bus.consumer("vertx-worker", msg->{
            try {
                Thread.currentThread().sleep(4000);
                msg.reply("Hello, " + msg.body() + "!!!");
            } catch (Exception ex) {
                System.out.println("Error happens ...");
            }
        });

    }
}
