/*
 * Copyright (C) 2021 AlenPaulVarghese <alenpaul2001@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Db;

import Http.Scaffold;
import java.sql.*;
import java.util.List;

/**
 *
 * @author AlenPaulVarghese <alenpaul2001@gmail.com>
 */
public class Database {

    static final String DBURI = "jdbc:mysql://localhost:3306/covid";
    static final String DBUSER = "corona";
    static final String DBPASS = "corona";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DBURI, DBUSER, DBPASS);
    }

    public static void updateCountries(List<Scaffold> countries) throws SQLException {
        Connection dbConn = getConnection();
        try {
            createTable(dbConn);
            for (Scaffold country : countries) {
                insertValue(dbConn, country);
            }

        } finally {
            dbConn.close();
        }
    }

    public static void createTable(Connection db) throws SQLException {
        PreparedStatement query = db.prepareStatement(
                "CREATE TABLE IF NOT EXISTS COVIDCHART "
                + "(countryname VARCHAR(50), "
                + "countrycode CHAR(2) UNIQUE, "
                + "confirmed INT, recovered INT, "
                + "death INT)");
        query.executeUpdate();
        query.close();
    }

    public static void insertValue(Connection db, Scaffold country) throws SQLException {
        PreparedStatement query = db.prepareStatement(
                "INSERT INTO COVIDCHART"
                + "(countryname, countrycode, confirmed, recovered, death) VALUES(?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "confirmed = VALUES(confirmed), "
                + "recovered = VALUES(recovered), "
                + "death = VALUES(death);");
        query.setString(1, country.countryName);
        query.setString(2, country.countryCode);
        query.setInt(3, country.totalConfirmed);
        query.setInt(4, country.totalRecovered);
        query.setInt(5, country.totalDeaths);
        query.executeUpdate();
        query.close();
    }

    public static ResultSet queryCountries(Connection db) throws SQLException {
        PreparedStatement query = db.prepareStatement("SELECT * from COVIDCHART;");
        return query.executeQuery();
    }

    public static ResultSet queryCountry(Connection db, String country) throws SQLException {
        PreparedStatement query = db.prepareStatement(
                "SELECT countryname, confirmed, recovered, death "
                + "from COVIDCHART where countryname = ?;");
        query.setString(1, country);
        return query.executeQuery();
    }

    public static ResultSet queryCountryNames(Connection db) throws SQLException {
        PreparedStatement query = db.prepareStatement("SELECT countryname from COVIDCHART;");
        return query.executeQuery();
    }

    public static void createDefaultSettings(Connection db) throws SQLException {
        PreparedStatement query = db.prepareStatement(
                "CREATE TABLE IF NOT EXISTS COVIDSETTINGS "
                + "(defaultcountry VARCHAR(50), "
                + "autorefresh BOOLEAN); ");
        query.execute();
        query = db.prepareStatement("INSERT INTO COVIDSETTINGS VALUES (?, ?)");
        query.setString(1, "Global");
        query.setInt(2, 0);
        query.executeUpdate();

    }

    public static void updateSettings(String countryname, boolean autorefresh, Connection db) throws SQLException {
        PreparedStatement query = db.prepareStatement("UPDATE COVIDSETTINGS "
                + "SET defaultcountry = ?, autorefresh = ?;");
        query.setString(1, countryname);
        query.setBoolean(2, autorefresh);
        query.executeUpdate();

    }

    public static ResultSet getSettings(Connection db, boolean retry) throws SQLException {
        try {
            PreparedStatement query = db.prepareStatement("select * from COVIDSETTINGS");
            return query.executeQuery();
        } catch (SQLSyntaxErrorException e) {
            createDefaultSettings(db);
            if (retry == false) {
                return getSettings(db, true);
            } else {
                throw new SQLException("cannot create database");
            }
        }
    }

    public static int getTotalCountries(Connection db) throws SQLException {
        PreparedStatement query = db.prepareStatement("SELECT COUNT(countryname) from COVIDCHART");
        ResultSet result = query.executeQuery();
        while (result.next()) {
            return result.getInt(1);
        }
        return 0;
    }

    public static ResultSet getIndex(Connection db) throws SQLException {
        PreparedStatement query = db.prepareStatement(
                "SELECT total.COUNT, top_confirmed.countryname, top_recovered.countryname, top_deaths.countryname  from "
                + "(SELECT COUNT(countryname) AS COUNT FROM COVIDCHART WHERE countryname <>'Global') AS total, "
                + "(SELECT countryname FROM COVIDCHART WHERE countryname <>'Global' ORDER BY confirmed DESC LIMIT 1) AS top_confirmed, "
                + "(SELECT countryname FROM COVIDCHART WHERE countryname <>'Global' ORDER BY recovered DESC LIMIT 1) AS top_recovered, "
                + "(SELECT countryname FROM COVIDCHART WHERE countryname <>'Global' ORDER BY death DESC LIMIT 1) AS top_deaths;");
        return query.executeQuery();
    }

    public static ResultSet filterModel(
            String field,
            String operator,
            int value,
            Connection db) throws SQLException {
        String operator_string = "";
        if (operator.equals("Lesser than")) {
            operator_string = "<";
        } else if (operator.equals("Equals to")) {
            operator_string = "=";
        } else {
            operator_string = ">";
        }
        PreparedStatement query = db.prepareStatement(
                String.format(
                        "SELECT * FROM COVIDCHART WHERE (%s %s ?) AND (countryname <>'Global') ORDER BY %s DESC;",
                        field,
                        operator_string,
                        field
                )
        );
        query.setInt(1, value);
        return query.executeQuery();
    }
}
