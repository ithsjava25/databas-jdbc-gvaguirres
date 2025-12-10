package com.example;

import java.sql.*;
import java.util.Arrays;
import java.lang.System.*;
import java.util.Scanner;

public class Main {

    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        // Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        Scanner sc = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(jdbcUrl, dbUser, dbPass)) {

            do{

                System.out.print("Username: ");
                var username = sc.nextLine();
                System.out.print("Password: ");
                var password = sc.nextLine();

                //LOGIN

                String query = "select count(*) from account where name = ? and password = ?";
                try(PreparedStatement statement = connection.prepareStatement(query)){
                    statement.setString(1, username);
                    statement.setString(2, password);

                    try(ResultSet result = statement.executeQuery()){
                        result.next();
                        if(result.getInt(1) > 0){
                            System.out.println("Login successful");

                            //SUCCESSFUL LOGIN CONTINUE TO MENU

                            do{
                                menu();

                                System.out.println("Enter an option:");
                                int option = sc.nextInt();

                                switch (option) {
                                    case 0: break;
                                    case 1: listMoonMissions(connection);
                                            break;
                                    case 2: moonMissionByMission_id(connection, sc);
                                            break;
                                    case 3: numberOfMissionsForAGivenYear(connection, sc);
                                            break;
                                    case 4: createAnAccount(connection, sc);
                                            break;
                                    case 5: updateAnAccountPassword(connection, sc);
                                            break;
                                    case 6: deleteAnAccount(connection, sc);
                                    default: break;

                                }

                            } while (true);

                        }

                        //INVALID LOGIN

                        else {
                            System.out.println("Invalid username or password.");
                            System.out.print("Enter 0 to exit");
                            String exit = sc.nextLine();
                            if (exit.equals("0"))
                                return;
                        }
                    }
                }
            }while(true);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void deleteAnAccount(Connection connection, Scanner sc) throws SQLException{
        // 6) Delete an account (prompts: user_id; prints confirmation).
        System.out.println("Delete an account");
        System.out.println("Enter user_id: ");
        long userId = sc.nextLong();

        var query = "delete from account where user_id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.executeUpdate();

            System.out.println("Deleted");
        }
    }

    private static void updateAnAccountPassword(Connection connection, Scanner sc) throws SQLException{
        // 5) Update an account password (prompts: user_id, new password; prints confirmation)
        System.out.println("Update an account password");
        System.out.println("Enter user_id: ");
        long userId = sc.nextLong();
        sc.nextLine();
        System.out.println("Enter new password:");
        String newPassword = sc.nextLine();

        var query = "update account set password = ? where user_id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, newPassword);
            statement.setLong(2, userId);
            statement.executeUpdate();

            System.out.println("New password updated: " + statement);
        }
    }

    private static void createAnAccount(Connection connection, Scanner sc) throws SQLException{
        // 4) Create an account (prompts: first name, last name, ssn, password; prints confirmation)
        System.out.println("Create an account");
        System.out.println("Enter first name: ");
        String firstName = sc.nextLine();
        System.out.println("Enter last name: ");
        String lastName = sc.nextLine();
        System.out.println("Enter ssn: ");
        String ssn = sc.nextLine();
        System.out.println("Enter password: ");
        String password = sc.nextLine();

        String query = "insert into account(first_name, last_name, ssn, password) values(?,?,?,?)";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setString(1, firstName);
            statement.setString(2, lastName);
            statement.setString(3, ssn);
            statement.setString(4, password);
            statement.executeUpdate();

            System.out.println("Account created");
        }
    }

    private static void numberOfMissionsForAGivenYear(Connection connection, Scanner sc) throws SQLException {
        // 3) Count missions for a given year (prompts: year; prints the number of missions launched that year).
        System.out.println("Enter a year: ");
        int year = sc.nextInt();
        String query = "select count(*) as number_of_missions from moon_mission where Year(launch_date) = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, year);
            ResultSet result = statement.executeQuery();
            if (result.next()){
                System.out.println("Number of missions for the year " + year + "is " + result.getInt("number_of_missions"));
            }
            result.close();

        }
    }

    private static void moonMissionByMission_id(Connection connection, Scanner sc) throws SQLException {
        // 2) Get a moon mission by mission_id (prints details for that mission).
        System.out.println("Enter a mission_id: ");
        int mission_id = sc.nextInt();
        String query = "select * from moon_mission where mission_id = ?";
        try(PreparedStatement statement = connection.prepareStatement(query)){
            statement.setInt(1, mission_id);
            ResultSet result = statement.executeQuery();
            while (result.next()){
                    System.out.println(result.getString(1));
                    System.out.println(result.getString(2));
                    System.out.println(result.getString(3));
                    System.out.println(result.getString(4));
                    System.out.println(result.getString(5));
                    System.out.println(result.getString(6));
                    System.out.println(result.getString(7));
            }
            result.close();
        }
    }

    private static void listMoonMissions(Connection connection) throws SQLException {
        // 1) List moon missions (prints spacecraft names from `moon_mission`).
        String query1 = "select spacecraft from moon_mission";
        try(PreparedStatement statement = connection.prepareStatement(query1)){
            ResultSet result = statement.executeQuery();
            while (result.next()){
                System.out.println(result.getString("spacecraft"));
            }
        }
    }

    private static void menu(){
        System.out.print(
                "Menu:\n" +
                "1) List moon missions\n" +
                "2) Get a moon mission by mission_id\n" +
                "3) Count missions for a given year\n" +
                "4) Create an account (prompts: first name, last name, ssn, password\n" +
                "5) Update an account password (prompts: user_id, new password; prints confirmation)\n" +
                "6) Delete an account (prompts: user_id; prints confirmation)\n" +
                "0) Exit\n");
    }

    /**
     * Determines if the application is running in development mode based on system properties,
     * environment variables, or command-line arguments.
     *
     * @param args an array of command-line arguments
     * @return {@code true} if the application is in development mode; {@code false} otherwise
     */
    private static boolean isDevMode(String[] args) {
        if (Boolean.getBoolean("devMode"))  //Add VM option -DdevMode=true
            return true;
        if ("true".equalsIgnoreCase(System.getenv("DEV_MODE")))  //Environment variable DEV_MODE=true
            return true;
        return Arrays.asList(args).contains("--dev"); //Argument --dev
    }

    /**
     * Reads configuration with precedence: Java system property first, then environment variable.
     * Returns trimmed value or null if neither source provides a non-empty value.
     */
    private static String resolveConfig(String propertyKey, String envKey) {
        String v = System.getProperty(propertyKey);
        if (v == null || v.trim().isEmpty()) {
            v = System.getenv(envKey);
        }
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
