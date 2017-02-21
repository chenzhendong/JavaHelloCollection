package com.mmm.his.nlpengine.resteasy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * Created by Zhendong Chen on 2/15/17.
 */
public class VertxMain extends HttpServlet {
    public static Vertx vertx;
    public void init(ServletConfig config) throws ServletException {
        DeploymentOptions options = new DeploymentOptions();
        vertx = Vertx.vertx();
        vertx.deployVerticle(new MessageWorker(), options.setWorker(true));
    }
}
