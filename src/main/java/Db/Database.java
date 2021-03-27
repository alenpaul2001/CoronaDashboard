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

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.List;
import Http.Scaffold;

/**
 *
 * @author AlenPaulVarghese <alenpaul2001@gmail.com>
 */
public class Database {
    static String dbURI = "jdbc:mysql://localhost:3306/covid";
    static String dbUser = "corona";
    static String dbPass = "corona";
    public static Connection getConnection() throws SQLException{
            return DriverManager.getConnection(dbURI, dbUser, dbPass);
    }
    
    public static void updateCountries(List<Scaffold> countries){
        try{
            Connection dbConn = getConnection();
            createTable(dbConn);
            try{
                countries.forEach((country) -> {
                    insertValue(dbConn, country);
                });
            } finally {
                dbConn.close();
            }
        } catch (Exception ex){
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
    
    public static void createTable(Connection db){
        try{
            PreparedStatement query = db.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS COVIDCHART "
                            + "(countryname VARCHAR(50), "
                            + "countrycode CHAR(2) UNIQUE, "
                            + "confirmed INT, recovered INT, "
                            + "death INT)");
            query.executeUpdate();
        } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public static void insertValue(Connection db, Scaffold country){
        try{
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
        } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
    }

    public static ResultSet queryCountries(Connection db){
        try{
            PreparedStatement query = db.prepareStatement("SELECT * from COVIDCHART;");
            return query.executeQuery();
        } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
    }

    public static ResultSet queryCountry(Connection db, String country){
       try{
            PreparedStatement query = db.prepareStatement(
                    "SELECT countryname, confirmed, recovered, death "
                            + "from COVIDCHART where countryname = ?;");
            query.setString(1, country);
            return query.executeQuery();
        } catch (SQLException ex) {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
    }
}
