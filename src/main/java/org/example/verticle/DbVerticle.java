package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.*;
import org.example.api.src.Customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static io.vertx.mysqlclient.MySQLPool.pool;

public class DbVerticle extends AbstractVerticle {
    private Router router;
    //private final MySQLPool connection;
    DbVerticle(Router router){
        this.router = router;
    }
    MySQLPool conn;
    private static Function<Row, Customer> MAPPER = (row) ->
            Customer.of(
                    //row.getInteger("CustomerID"),
                    row.getString("CustomerName"),
                    row.getString("City"),
                    row.getString("Country")
            );

    public Future<List<Customer>> findAll(){

        return conn.query("SELECT * FROM Sample.Customers")
                .execute()
                .map(rs -> StreamSupport.stream(rs.spliterator(),false)
                        .map(MAPPER)
                        .collect(Collectors.toList())
                );
    }
    public Future<Integer> save(JsonObject data) {
        List<Tuple> batch = new ArrayList<>();
        batch.add(Tuple.of(data.getString("CustomerName"),data.getString("City"),data.getString("Country")));
        return conn.preparedQuery("INSERT INTO Sample.Customers(CustomerName, City, Country) VALUES (?, ?, ?);")
                .executeBatch(batch)
                .map(SqlResult::rowCount);
    }
    public Future<Integer> deleteByID(String id){
        Objects.requireNonNull(id, "id cannot be NULL");
//        String query = "DELETE FROM Sample.Customers WHERE id = #{id}";

        return conn.preparedQuery("DELETE FROM Sample.Customers WHERE CustomerID=?")
                .execute(Tuple.of(id))
                .map(SqlResult::rowCount);
    }

    private MySQLPool initDB() {
        MySQLConnectOptions connectOptions = new MySQLConnectOptions()
                .setPort(3306)
                .setHost("localhost")
                .setDatabase("Sample")
                .setUser("root")
                .setPassword("Admin@123");

        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);

        MySQLPool pool = pool(vertx,connectOptions, poolOptions);
        return pool;
    }

    public void start() throws Exception {
        System.out.println("hello from DB verticle");
        conn = initDB();
        /*
        GET from DB
         */
        EventBus eveBus = vertx.eventBus();
        eveBus.consumer("GET", message -> {
            if (message.body() == "GET"){
                this.findAll()
                        .onSuccess(result ->{
                            System.out.println("Done!");
                            EventBus evBus = vertx.eventBus();
                            evBus.send("GET.res", Json.encodePrettily(result));
                        })
                        .onFailure(
                                throwable -> eveBus.send("GET.res",throwable.getMessage())
                        );
            }
        });
        /*
        UPDATE DB
         */
        eveBus.consumer("POST",message -> {
            var body = message.body();
            JsonObject jsonBody = (JsonObject) body;
            //String id = jsonBody.getString("CustomerID");
           this.save(jsonBody)
                   .onSuccess(result->{
                       eveBus.send("POST.res",Json.encodePrettily(result));
                   })
                   .onFailure(
                           throwable -> eveBus.send("POST.res",throwable.getMessage())
                   );
        });
        /*
        DELETE from DB
         */
        eveBus.consumer("DELETE",message->{
            var body = message.body();
            String id = (String) body;
            System.out.println(id);
            this.deleteByID(id)
                    .onSuccess(result->{
                        eveBus.send("DELETE.res",Json.encodePrettily(result));
                    })
                    .onFailure(
                            throwable -> eveBus.send("DELETE.res",throwable.getMessage())
                    );
        });
    }
}
