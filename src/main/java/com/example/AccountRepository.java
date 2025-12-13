package com.example;

import java.util.Map;

public interface AccountRepository {

    boolean login(String username, String password);
    void createAnAccount(String firstName, String lastName, String ssn, String password);
    void updateAnAccountPassword(long userId, String newPassword);
    void deleteAnAccount(long userId);
}
