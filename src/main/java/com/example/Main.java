package com.example;

import java.sql.*;
import java.util.Arrays;
import java.lang.System.*;
import java.util.List;
import java.util.Scanner;

public class Main {

    static void main(String[] args) {
        if (isDevMode(args)) {
            DevDatabaseInitializer.start();
        }
        new Main().run();
    }

    public void run() {
        //Resolve DB settings with precedence: System properties -> Environment variables
        String jdbcUrl = resolveConfig("APP_JDBC_URL", "APP_JDBC_URL");
        String dbUser = resolveConfig("APP_DB_USER", "APP_DB_USER");
        String dbPass = resolveConfig("APP_DB_PASS", "APP_DB_PASS");

        if (jdbcUrl == null || dbUser == null || dbPass == null) {
            throw new IllegalStateException(
                    "Missing DB configuration. Provide APP_JDBC_URL, APP_DB_USER, APP_DB_PASS " +
                            "as system properties (-Dkey=value) or environment variables.");
        }

        DataSource dataSource = new DataSource(jdbcUrl, dbUser, dbPass);
        MoonMissionRepository repository = new MoonMissionRepositoryImpl(dataSource);
        AccountRepository accountRepository = new AccountRepositoryImpl(dataSource);

        Scanner sc = new Scanner(System.in);

        try {

            do {
                boolean login = login(accountRepository, sc);

                if (login) {
                    System.out.println("Login successful");

                    //SUCCESSFUL LOGIN CONTINUE TO MENU

                    do {
                        menu();

                        System.out.println("Enter an option:");
                        int option;
                        try {
                            option = Integer.parseInt(sc.nextLine());
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid option");
                            continue;
                        }

                        switch (option) {
                            case 0:
                                 dataSource.close();
                                 return;
                            case 1:
                                listMoonMissions(repository);
                                break;
                            case 2:
                                moonMissionByMission_id(repository, sc);
                                break;
                            case 3:
                                numberOfMissionsForAGivenYear(repository, sc);
                                break;
                            case 4:
                                createAnAccount(accountRepository, sc);
                                break;
                            case 5:
                                updateAnAccountPassword(accountRepository, sc);
                                break;
                            case 6:
                                deleteAnAccount(accountRepository, sc);
                                break;
                            default:
                                dataSource.close();
                                break;

                        }
                    }
                    while (true);

                } else {

                    //INVALID LOGIN

                    System.out.println("Invalid username or password.");
                    System.out.print("Enter 0 to exit");
                    String exit = sc.nextLine();
                    if (exit.equals("0"))
                        return;
                }
            }  while (true);

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }

    private static void deleteAnAccount(AccountRepository accountRepository, Scanner sc) throws SQLException {
        // 6) Delete an account (prompts: user_id; prints confirmation).
        System.out.println("Delete an account");
        System.out.println("Enter user_id: ");
        long userId = sc.nextLong();
        sc.nextLine();

        accountRepository.deleteAnAccount(userId);
        System.out.println("Account deleted");

    }

    private static void updateAnAccountPassword(AccountRepository accountRepository, Scanner sc) throws SQLException {
        // 5) Update an account password (prompts: user_id, new password; prints confirmation)
        System.out.println("Update an account password");
        System.out.println("Enter user_id: ");
        long userId = sc.nextLong();
        sc.nextLine();
        System.out.println("Enter new password:");
        String newPassword = sc.nextLine();

        accountRepository.updateAnAccountPassword(userId, newPassword);
        System.out.println("Password updated");

    }

    private static void createAnAccount(AccountRepository accountRepository, Scanner sc) throws SQLException {
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

        accountRepository.createAnAccount(firstName, lastName, ssn, password);
        System.out.println("Account created");
    }

    private static void numberOfMissionsForAGivenYear(MoonMissionRepository repository, Scanner sc) throws SQLException {
        // 3) Count missions for a given year (prompts: year; prints the number of missions launched that year).
        System.out.println("Enter a year: ");
        int year = sc.nextInt();
        sc.nextLine();

        int numberOfMissionsForAGivenYear = repository.numberOfMissionsForAGivenYear(year);

        if (numberOfMissionsForAGivenYear != 0) {
            System.out.println("Number of missions for the year " + year + " is " + numberOfMissionsForAGivenYear);
        } else {
            System.out.println("The number of missions for that year is 0");
        }
    }

    private static void moonMissionByMission_id(MoonMissionRepository repository, Scanner sc) throws SQLException {
        // 2) Get a moon mission by mission_id (prints details for that mission).

        System.out.println("Enter a mission_id: ");
        int mission_id = sc.nextInt();
        sc.nextLine();

        String moonMissionDetails = repository.moonMissionByMission_id(mission_id);

        if (moonMissionDetails != null) {
            System.out.println("Details: ");
            System.out.println(moonMissionDetails);
        } else {
            System.out.println("Mission not found.");
        }
    }

    private static void listMoonMissions(MoonMissionRepository repository) throws SQLException {
        // 1) List moon missions (prints spacecraft names from `moon_mission`).

        List<String> listMoonMissions = repository.listMoonMissions();

        if (listMoonMissions != null)
            System.out.println(listMoonMissions);
    }

    private static void menu() {
        System.out.print(
                "Menu:\n" +
                        "1) List moon missions\n" +
                        "2) Get a moon mission by mission_id\n" +
                        "3) Count missions for a given year\n" +
                        "4) Create an account (prompts: first name, last name, ssn, password)\n" +
                        "5) Update an account password (prompts: user_id, new password; prints confirmation)\n" +
                        "6) Delete an account (prompts: user_id; prints confirmation)\n" +
                        "0) Exit\n");
    }

    private static boolean login(AccountRepository accountRepository, Scanner sc) throws SQLException{

        System.out.print("Username: ");
        String username = sc.nextLine();
        System.out.print("Password: ");
        String password = sc.nextLine();

        boolean login = accountRepository.login(username, password);

        return login;
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

