package ru.gb.server.auth;

import java.sql.*;

public class AuthenticationService {

    private static Connection connection;
    private static Statement statement;

    public static void runBD() {
        try {
            connect();
            dropTable();
            createTable();
            insert("l1", "p1", "n1");
            insert("l2", "p2", "n2");
            insert("l3", "p3", "n3");
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }

    static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:maindb.db");
        statement = connection.createStatement();
    }

    static void createTable() throws SQLException {
        try {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS clients (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "login TEXT UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "nickname TEXT NOT NULL" +
                    ");");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static void insert(final String login, final String password, final String nickname) throws SQLException {
        try (final PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO clients " +
                "(login, password, nickname) VALUES (?, ?, ?);")) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            preparedStatement.setString(3, nickname);
            preparedStatement.executeUpdate();
        }
    }

    static String getNickByLoginPass(String login, String password) throws SQLException {
        connect();
        try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT nickname FROM clients " +
                "WHERE login = ? AND password = ?;")) {
            preparedStatement.setString(1, login);
            preparedStatement.setString(2, password);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        }
    }

    public static String getLoginByNickname(String nickname) throws SQLException {
        connect();
        try (final PreparedStatement preparedStatement = connection.prepareStatement("SELECT login FROM clients " +
                "WHERE nickname = ?;")) {
            preparedStatement.setString(1, nickname);
            final ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                return resultSet.getString(1);
            }
            return null;
        }
    }

    public static void changeNickname(String nickname, String login) throws SQLException {
        connect();
        try (final PreparedStatement preparedStatement = connection.prepareStatement("UPDATE clients SET nickname = ?" +
                " WHERE login = ?;")) {
            preparedStatement.setString(1, nickname);
            preparedStatement.setString(2, login);
            preparedStatement.executeUpdate();
        }
        disconnect();
    }

    public static void printDB() throws SQLException {
        connect();
        try (final PreparedStatement preparedStatement =
                     connection.prepareStatement("SELECT * FROM clients;")) {
            final ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                System.out.printf("id - %d | login - %s | password - %s | nickname - %s\n",
                        resultSet.getInt(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4));
            }
        }
    }

    public static void dropTable() throws SQLException {
        try {
            statement.executeUpdate("DROP TABLE IF EXISTS clients");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        try {
            if (statement != null) {
                statement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
