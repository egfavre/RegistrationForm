package com.egfavre;

import org.junit.Test;

import java.sql.*;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by user on 6/15/16.
 */
public class MainTest {
    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }

    @Test
    public void testInsertAndSelect() throws SQLException {
        Connection conn = startConnection();
        User user = new User(1, "test", "test", "test");
        Main.insertUser(conn, user);
        ArrayList<User> testList = Main.selectAllUsers(conn);
        conn.close();

        assertTrue(testList.size() > 0);
    }

    @Test
    public void testUpdate() throws SQLException {
        Connection conn = startConnection();
        User user = new User(1, "test", "test", "test");
        Main.insertUser(conn, user);
        Main.updateUsers(conn, "testN", "testA", "testE", 1);
        user = Main.selectOneUser(conn, "testN");

        conn.close();

        assertTrue(user.address.equals("testA"));
    }

    @Test
    public void testDelete() throws SQLException{
        Connection conn = startConnection();
        User user = new User(1, "test", "test", "test");
        Main.insertUser(conn, user);
        Main.deleteUser(conn, 1);
        ArrayList testList = Main.selectAllUsers(conn);

        conn.close();
        assertTrue(testList.size() == 0);
    }
}