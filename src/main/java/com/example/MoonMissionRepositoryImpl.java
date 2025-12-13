package com.example;

import java.sql.*;
import java.util.*;

public class MoonMissionRepositoryImpl implements MoonMissionRepository{

    private final DataSource dataSource;

    public MoonMissionRepositoryImpl(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Override
    public List<String> listMoonMissions() {
        // 1) List moon missions (prints spacecraft names from `moon_mission`).

        List<String> spacecrafts = new ArrayList<>();
        String query = "select spacecraft from moon_mission";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                spacecrafts.add(result.getString("spacecraft"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return spacecrafts;
    }

    @Override
    public String moonMissionByMission_id (int mission_id) {
        // 2) Get a moon mission by mission_id (prints details for that mission).

        String query = "select * from moon_mission where mission_id = ?";

        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query);
        ) {
            statement.setInt(1, mission_id);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return String.format("mission_id: %s, Spacecraft: %s, Launch Date: %s, Carrier Rocket: %s, Operator: %s, Mission Type: %s, Outcome: %s",
                            result.getString(1),
                            result.getString(2),
                            result.getString(3),
                            result.getString(4),
                            result.getString(5),
                            result.getString(6),
                            result.getString(7));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

        @Override
    public int numberOfMissionsForAGivenYear(int year){
        // 3) Count missions for a given year (prompts: year; prints the number of missions launched that year).

        String query = "select count(*) as number_of_missions from moon_mission where Year(launch_date) = ?";

        try(
                Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(query)
        ) {
                statement.setInt(1, year);

                try(ResultSet result = statement.executeQuery()) {
                      if (result.next()){
                         return result.getInt("number_of_missions");
                      }
                }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }
}

