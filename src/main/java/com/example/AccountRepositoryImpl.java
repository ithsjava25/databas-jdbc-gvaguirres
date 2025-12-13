package com.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class AccountRepositoryImpl implements AccountRepository {

    private final DataSource dataSource;

    public AccountRepositoryImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public boolean login(String username, String password) {

        String query = "select count(*) from account where name = ? and password = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()){
                    return result.getInt(1) > 0;
                }
                return false;
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createAnAccount(String firstName, String lastName, String ssn, String password) {

        String query = "insert into account(first_name, last_name, ssn, password) values(?,?,?,?)";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, ssn);
            statement.setString(4, password);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateAnAccountPassword(long userId, String newPassword) {

        var query = "update account set password = ? where user_id = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
            statement.setString(1, newPassword);
            statement.setLong(2, userId);
            statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteAnAccount(long userId) {

        var query = "delete from account where user_id = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ){
            statement.setLong(1, userId);
            int rowsAffected = statement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
}