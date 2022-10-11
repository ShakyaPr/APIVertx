package org.example.utils;

public class DbUtils {
//    MySQLConnectOptions connectOptions = new MySQLConnectOptions()
//            .setPort(3306)
//            .setHost("localhost")
//            .setDatabase("Sample")
//            .setUser("root")
//            .setPassword("Admin@123");
//
//    // Pool options
//    PoolOptions poolOptions = new PoolOptions()
//            .setMaxSize(5);
//
//    Vertx vertx = Vertx.vertx();
//
//    // Create the pooled client
//    MySQLPool pool = (MySQLPool) MySQLPool.pool(vertx, connectOptions, poolOptions);
//    pool.getConnection()
//            .compose(conn -> {
//                System.out.printf("Got a connection from the pool");
//                return conn
//                        .query("SELECT * FROM users WHERE id='julien'")
//                        .execute()
//                        .compose(res -> conn
//                                .query("SELECT * FROM users WHERE id='emad'")
//                                .execute())
//                        .onComplete(ar -> {
//                            // Release the connection to the pool
//                            conn.close();
//            }).onComplete(ar -> {
//                if (ar.succeeded()) {
//                    System.out.println("Done");
//                } else {
//                    System.out.println("Something went wrong " + ar.cause().getMessage());
//                }
//            });
}