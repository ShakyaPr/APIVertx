package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;

public class ServerVerticle extends AbstractVerticle {
    private Router router;
    private ServerVerticle(Router router){
        this.router = router;
    }

    static HttpServer server;

    public void start() throws Exception{

        EventBus eventBus = vertx.eventBus();

        server = vertx.createHttpServer(new HttpServerOptions().setPort(8888).setHost("localhost"));

        server.requestHandler(router)
                .listen(8888)
                .onSuccess(sv -> {
                    System.out.println("HTTP server started");
                })
                .onFailure(event->{
                    System.out.println("Failed to start");;
                });
    /*
     * GET request
     */
        router.get("/getAll").produces("application/json")
                .handler(req ->{
                    eventBus.send("GET","GET");
                    eventBus.consumer("GET.res",res->{
                        req.response()
                                .end(Json.encodePrettily(res.body()));
                    });
                });
    /*
     * POST request
     */
        router.post("/postEmp")
                .handler(BodyHandler.create())
                .handler(req->{
                    var body = req.getBodyAsJson();
                    eventBus.send("POST",body);
                    eventBus.consumer("POST.res",res->{
                        if (res.body().toString().equals("1")){
                            System.out.println("Updated");
                            req.response().end("Successfully inserted new customer!");
                        } else{
                            req.response().end(Json.encodePrettily(res.body()));
                        }
                    });
                    //String id = body.getString("CustomerID");
                });
        /*
        DELETE request
         */
        router.delete("/delEmp/:id")
                .handler(req->{
                    var params = req.pathParams();
                    var id = params.get("id");
                    eventBus.send("DELETE",id);
                    eventBus.consumer("DELETE.res",res->{
                        if (res.body().toString().equals("1")){
                            req.response().end("Successfully deleted!");
                        } else if(res.body().toString().equals("0")){
                            req.response().end("No such ID");
                        } else{
                            req.response().end(Json.encodePrettily(res.body()));
                        }
                    });
                });

        /*
        PUT request
         */
        router.put("/putEmp/:id")
                .handler(BodyHandler.create())
                .handler(req->{
                    var body = req.getBodyAsJson();
                    var params = req.pathParams();
                    var id = params.get("id");
                    body.put("id",id);
                    eventBus.send("PUT",body);
                    eventBus.consumer("PUT.res",res->{
                        if (res.body().toString().equals("1")){
                            req.response().end("Successfully updated!");
                        } else if(res.body().toString().equals("0")){
                            req.response().end("No such ID in DB");
                        } else{
                            req.response().end(Json.encodePrettily(res.body()));
                        }
                    });
                });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        Router router = Router.router(vertx);
        vertx.deployVerticle(new ServerVerticle(router));
        vertx.deployVerticle(new DbVerticle(router));
    }
}
