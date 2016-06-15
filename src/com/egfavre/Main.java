package com.egfavre;

import jodd.json.JsonParser;
import jodd.json.JsonSerializer;
import org.h2.tools.Server;
import spark.Spark;

import java.sql.*;
import java.util.ArrayList;

public class Main {

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS users (id IDENTITY, username VARCHAR, address VARCHAR, email VARCHAR)");
    }

    public static void insertUser(Connection conn, User user) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO users VALUES (NULL, ?, ?, ?)");
        stmt.setString(1, user.username);
        stmt.setString(2, user.address);
        stmt.setString(3, user.email);
        stmt.execute();
    }

    public static ArrayList<User> selectAllUsers(Connection conn) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users");
        ResultSet results = stmt.executeQuery();
        ArrayList<User> userList = new ArrayList<>();
        while (results.next()) {
            int id = results.getInt("id");
            String username = results.getString("username");
            String address = results.getString("address");
            String email = results.getString("email");
            User user = new User(id, username, address, email);
            userList.add(user);
        }
        return userList;
    }

    public static User selectOneUser(Connection conn, String username) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?");
        stmt.setString(1, username);
        ResultSet results = stmt.executeQuery();
        User user = new User();
        while (results.next()) {
            int id = results.getInt("id");
            String address = results.getString("address");
            String email = results.getString("email");
            user.username = username;
            user.address = address;
            user.email = email;
        }
        return user;
    }


    public static void updateUsers(Connection conn, User user, Integer userId) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("UPDATE users SET username = ? WHERE id = ?");
        PreparedStatement stmt1 = conn.prepareStatement("UPDATE users SET address = ? WHERE id = ?");
        PreparedStatement stmt2 = conn.prepareStatement("UPDATE users SET email = ? WHERE id = ?");
        stmt.setString(1, user.username);
        stmt1.setString(1, user.address);
        stmt2.setString(1, user.email);
        stmt.setInt(2, userId);
        stmt1.setInt(2, userId);
        stmt2.setInt(2, userId);
        stmt.execute();
        stmt1.execute();
        stmt2.execute();
    }

    public static void deleteUser(Connection conn, Integer id) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id = ?");
        stmt.setInt(1, id);
        stmt.execute();
    }

    public static void main(String[] args) throws SQLException {
        //Connect to the database and create a table with four columns: id, username, address, and email.
        Server.createWebServer().start();
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        Spark.externalStaticFileLocation("public");
        Spark.init();

        //Create a GET route called /user that calls selectUsers and returns the data as JSON.
        Spark.get(
                "/user",
                (request, response) -> {
                    ArrayList<User> userList = selectAllUsers(conn);
                    JsonSerializer s = new JsonSerializer();
                    return s.serialize(userList);
                }
        );

        //Create a POST route called /user that parses request.body() into a User object
        // and calls insertUser to put it in the database.
        Spark.post(
                "/user",
                (request, response) -> {
                    String body = request.body();
                    //body contains json
                    JsonParser p = new JsonParser();
                    //parse body and add to message.class
                    User user = p.parse(body, User.class);
                    insertUser(conn, user);
                    return "";
                }
        );

        //Create a PUT route called /user that parses request.body() into a User object
        // and calls updateUser to update it in the database.
        Spark.put(
                "/user",
                (request, response) -> {
                    String body = request.body();
                    JsonParser p = new JsonParser();
                    User user = p.parse(body, User.class);
                    updateUsers(conn, user, user.id);
                    return "";
                }
        );

        //Create a DELETE route called /user/:id that gets the id via request.params(":id")
        // and gives it to deleteUser to delete it in the database.
        Spark.delete(
                "/user/:id",
                (request, response) -> {
                    int id = Integer.valueOf(request.params(":id"));
                    deleteUser(conn, id);
                    return "";
                }
        );
    }

}