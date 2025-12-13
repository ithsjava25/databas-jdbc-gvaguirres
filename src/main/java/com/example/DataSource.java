package com.example;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;


public class DataSource {

    private static HikariDataSource ds;

    public DataSource(String jdbcUrl, String dbUser, String dbPass){
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPass);
        config.addDataSourceProperty( "cachePrepStmts" , "true" );
        config.addDataSourceProperty( "prepStmtCacheSize" , "250" );
        config.addDataSourceProperty( "prepStmtCacheSqlLimit" , "2048" );
        ds = new HikariDataSource(config);

    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public void close(){
        if (ds != null){
            ds.close();
        }
    }
}

