package com.example;

import java.sql.SQLException;
import java.util.List;

public interface MoonMissionRepository {

    List<String> listMoonMissions ();
    String moonMissionByMission_id (int mission_id);
    int numberOfMissionsForAGivenYear (int year);
}
