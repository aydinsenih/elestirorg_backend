package com.elestir.Elestirorg;

import java.sql.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.google.appengine.api.utils.SystemProperty;

public class DatabaseConnection {
    private final String dbLocalURL = System.getenv("localsql");
    private final String dbCloudURL = System.getenv("cloudsql");
    private Connection conn = null;

    private boolean dbConnection(){
        try {
            if(SystemProperty.Environment.Value.Production == SystemProperty.environment.value()){
                conn = DriverManager.getConnection(dbCloudURL);
            }
            else{
                conn = DriverManager.getConnection(dbLocalURL,"root","root");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    protected List login(String username, String password){
        final String LOGIN_QUERY = "SELECT * FROM users WHERE username= ? AND password= ?";
        if(!dbConnection()){
            return new ArrayList(Arrays.asList("sql connection error."));
            }
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            preparedStatement = conn.prepareStatement(LOGIN_QUERY);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();

            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();
            ArrayList list = new ArrayList();
            while (resultSet.next()){
                HashMap hasmap = new HashMap(columns);
                for(int i=1; i<=columns ; ++i){
                    hasmap.put(md.getColumnName(i), resultSet.getObject(i));
                }
                list.add(hasmap);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList(Arrays.asList("sql connection error."));
        } finally { //connection close
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new ArrayList(Arrays.asList("sql connection error."));
                }
            }
        }
    }

    protected List signup(String username, String email, String password, String phonenumber){
        final String USERNAME_EMAIL_CHECK_QUERY = "SELECT * FROM users WHERE email= ? OR username= ?";
        final String SIGNUP_QUERY = "INSERT INTO users(username, email, password, phoneNumber, hasPermission) VALUES ( ?, ?, ?, ?, 1)";
        PreparedStatement ps = null;
        PreparedStatement ps2 = null;
        ResultSet rs = null;
        int updateResult = -1;

        if(!dbConnection()){
            return new ArrayList(Arrays.asList("sql connection error."));
        }

        try {
            ps = conn.prepareStatement(USERNAME_EMAIL_CHECK_QUERY);
            ps.setString(1,email);
            ps.setString(2,username);
            rs = ps.executeQuery();
            if(rs.next()){
                return new ArrayList(Arrays.asList("username or email in use."));
            }
            ps2 = conn.prepareStatement(SIGNUP_QUERY);
            ps2.setString(1, username);
            ps2.setString(2, email);
            ps2.setString(3, password);
            ps2.setString(4, phonenumber);
            updateResult = ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(updateResult == 1)
            return new ArrayList(Arrays.asList(email,username,"signup succesful"));
        return new ArrayList(Arrays.asList("signup error"));

    }

}
