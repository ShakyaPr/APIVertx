package org.example.verticle;

import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

public class PostHandler {

    DbVerticle db;

    public static PostHandler create(DbVerticle db){
        return new PostHandler(db);
    }

    private PostHandler(DbVerticle db){
        this.db = db;
    }

    public void all(RoutingContext rc){
        this.db.findAll()
                .onSuccess(
                        data -> rc.response().end(Json.encodePrettily(data))
                );

    }
}
