package org.example.verticle;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
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

        return conn.preparedQuery("DELETE FROM Sample.Customers WHERE CustomerID=?")
                .execute(Tuple.of(id))
                .map(SqlResult::rowCount);
    }
    public Future<Integer> update(JsonObject data){
        List<Tuple> batch = new ArrayList<>();
        batch.add(Tuple.of(data.getString("CustomerName"),data.getString("City"),data.getString("Country"), data.getString("id")));

        return conn.preparedQuery("UPDATE Sample.Customers SET CustomerName=?, City=?, Country=? WHERE CustomerID=?;")
                .executeBatch(batch)
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
    private <T> void all(Message<T> msg){
           this.findAll()
                        .onSuccess(result ->{
                            System.out.println("Done!");
                            EventBus evBus = vertx.eventBus();
                            msg.reply(Json.encodePrettily(result));
                        })
                   .onFailure(
                           throwable -> msg.reply(throwable.getMessage())
                   );
    }
    private <T> void post(Message<T> msg){
        var body = msg.body();
        JsonObject jsonBody = (JsonObject) body;
        this.save(jsonBody)
                .onSuccess(result->{
                    msg.reply(Json.encodePrettily(result));
                })
                .onFailure(
                        throwable -> msg.reply(throwable.getMessage())
                );
    }
    private <T> void delete(Message<T> msg){
        var body = msg.body();
        String id = (String) body;
        this.deleteByID(id)
                .onSuccess(result->{
                    msg.reply(Json.encodePrettily(result));
                })
                .onFailure(
                        throwable -> msg.reply(throwable.getMessage())
                );
    }
    private <T> void put(Message<T> msg){
        var body = msg.body();
        JsonObject jsonBody = (JsonObject) body;
        this.update(jsonBody)
                .onSuccess(result->{
                    msg.reply(Json.encodePrettily(result));
                })
                .onFailure(
                        throwable -> msg.reply(throwable.getMessage())
                );
    }
    public void start() throws Exception {
        System.out.println("hello from DB verticle");
        conn = initDB();

        EventBus eveBus = vertx.eventBus();

        // GET from DB

        eveBus.consumer("GET",this::all);

        // INSERT DB

        eveBus.consumer("POST",this::post);

        // DELETE from DB

        eveBus.consumer("DELETE",this::delete);

        // UPDATE DB

        eveBus.consumer("PUT",this::put);
    }
}
