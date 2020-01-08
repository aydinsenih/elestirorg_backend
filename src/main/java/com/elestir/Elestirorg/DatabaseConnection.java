package com.elestir.Elestirorg;

import java.sql.*;

import java.util.*;

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
            return true;
        }
        return false;
    }

    protected List<Object> login(String username, String password){
        final String LOGIN_QUERY = "SELECT ID, username, email, phoneNumber, avatar, creationTime, token FROM users WHERE username= ? AND password= ?";
        if(dbConnection()){
            return null;//if connection error occur.
            }
        PreparedStatement preparedStatement;
        ResultSet resultSet;
        try {
            preparedStatement = conn.prepareStatement(LOGIN_QUERY);
            preparedStatement.setString(1, username);
            preparedStatement.setString(2, password);
            resultSet = preparedStatement.executeQuery();

            ResultSetMetaData md = resultSet.getMetaData();
            int columns = md.getColumnCount();
            ArrayList<Object> list = new ArrayList<>();

            while (resultSet.next()){
                HashMap<String,Object> hashMap = new HashMap<>(columns);
                for(int i=1; i<=columns ; ++i){
                    hashMap.put(md.getColumnName(i), resultSet.getObject(i));
                }
                list.add(hashMap);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;//if connection error occur.
        } finally { //connection close
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //return null;//if connection error occur.
                }
            }
        }
    }

    protected List<Object> signup(String username, String email, String password, String phonenumber){
        final String USERNAME_EMAIL_CHECK_QUERY = "SELECT * FROM users WHERE email= ? OR username= ?";
        final String SIGNUP_QUERY = "INSERT INTO users(username, email, password, phoneNumber, hasPermission) VALUES ( ?, ?, ?, ?, 1)";
        PreparedStatement ps;
        PreparedStatement ps2;
        ResultSet rs;
        int updateResult = -1;

        if(dbConnection()){
            return null;//if connection error occur. //new ArrayList(Arrays.asList("sql connection error."));
        }

        try {
            ps = conn.prepareStatement(USERNAME_EMAIL_CHECK_QUERY);
            ps.setString(1,email);
            ps.setString(2,username);
            rs = ps.executeQuery();
            if(rs.next()){
                return new ArrayList(Collections.singletonList("Username or Email in use."));
            }
            ps2 = conn.prepareStatement(SIGNUP_QUERY);
            ps2.setString(1, username);
            ps2.setString(2, email);
            ps2.setString(3, password);
            ps2.setString(4, phonenumber);
            updateResult = ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //return null;//if connection error occur.
                }
            }
        }
        if(updateResult == 1)
            return new ArrayList(Arrays.asList(username,"Sign-up successful"));
        return new ArrayList(Arrays.asList("Sign-up error"));

    }

    public List logout(String token, String username){
        final String DELETE_TOKEN_QUERY = "UPDATE users SET users.token = null WHERE users.username = ?";
        PreparedStatement ps;
        int deleteResult = -1;
        if(dbConnection()){
            return new ArrayList(Arrays.asList("SQL connection error."));
        }

        try {
            ps = conn.prepareStatement(DELETE_TOKEN_QUERY);
            ps.setString(1,username);
            deleteResult = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //return new ArrayList(Arrays.asList("sql connection error."));
                }
            }
        }

        if (deleteResult == 1){
            return new ArrayList(Arrays.asList(username,"Logged-out."));
        }
        return new ArrayList(Arrays.asList("SQL connection error. Could not logged-out."));
    }


    public List updateTokenForUser(String username, String token) {
        final String UPDATE_TOKEN_QUERY = "UPDATE users SET users.token = ? WHERE users.username = ?";
        PreparedStatement ps;
        int updateResult = -1;

        if(dbConnection()){
            return new ArrayList(Arrays.asList("SQL connection error."));
        }

        try {
            ps = conn.prepareStatement(UPDATE_TOKEN_QUERY);
            ps.setString(1, token);
            ps.setString(2, username);
            updateResult = ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //return new ArrayList(Arrays.asList("sql connection error."));
                }
            }
        }
        if (updateResult == 1){
            return new ArrayList(Arrays.asList(username,"token updated."));
        }
        return new ArrayList(Arrays.asList("SQL connection error. Could not update or create token."));
    }

    public List<String> createQuestion(int userID, String question, String category, String a1, String a2, String a3, String a4, String a5){
        final String CREATE_QUESTION_QUERY =  "INSERT INTO `questions` (`question`, `userID`, `CategoryID`," +
                " `answer1`, `answer2`, `answer3`, `answer4`, `answer5`) VALUES (?, ?," +
                " (SELECT CategoryID from category WHERE category.categoryName = ?), ?, ?, ?, ?, ?)";
        PreparedStatement ps;
        int result = -1;

        if(dbConnection()){
            return new ArrayList(Arrays.asList("SQL connection error."));
        }
        try {
            ps = conn.prepareStatement(CREATE_QUESTION_QUERY);
            ps.setString(1, question);
            ps.setInt(2, userID);
            ps.setString(3, category);
            ps.setString(4, a1);
            ps.setString(5, a2);
            ps.setString(6, a3);
            ps.setString(7, a4);
            ps.setString(8, a5);
            result = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        if (result == 1){
            return new ArrayList(Arrays.asList("success","question created."));
        }
        return new ArrayList(Arrays.asList("SQL connection error. Could not create question."));
    }

    public List getQuestions(int offset, int count,int userID){
        //String GET_QUESTIONS_QUERY = "SELECT * from questions ORDER BY questions.ID DESC LIMIT ?,?";
        String GET_QUESTIONS_QUERY = "SELECT questions.*, users.username, users.avatar FROM questions " +
                "RIGHT JOIN users ON questions.userID = users.ID ORDER BY questions.ID DESC LIMIT ?, ?";
        PreparedStatement ps;
        ResultSet rs;
        if (dbConnection()){
            return new ArrayList(Arrays.asList("SQL connection error."));
        }
        ArrayList list = null;

        try {
            ps =conn.prepareStatement(GET_QUESTIONS_QUERY);
            ps.setInt(1, offset);
            ps.setInt(2, count);
            rs = ps.executeQuery();

            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            list = new ArrayList();
            while (rs.next()){
                HashMap hashmap = new HashMap(columns);
                HashMap answersMap;
                HashMap countsMap = new HashMap();
                HashMap userMap = new HashMap();
                ArrayList answersList = new ArrayList(5);
                if (userID != 0) {
                    int choice = getChoice(userID, rs.getInt("ID"));//TODO:tek ifle yap
                    if (choice != -1)
                        hashmap.put("choice", choice);
                    else{
                        hashmap.put("choice", null);
                    }
                } else{
                    hashmap.put("choice", null);
                }

                for(int i=1; i<=columns ; ++i){
                    String columnName = md.getColumnName(i);
                    answersMap = new HashMap();
                    if (columnName.startsWith("answer")){
                        answersMap.put("name", md.getColumnName(i));
                        answersMap.put("value", rs.getString(i));
                        answersList.add(answersMap);
                    } else if (columnName.contains("Count") || columnName.contains("Time")){
                        countsMap.put(md.getColumnName(i), rs.getObject(i));
                    } else if (columnName.equals("username") || columnName.equals("avatar") || columnName.equals("userID")){
                        userMap.put(md.getColumnName(i), rs.getObject(i));
                    } else {
                        hashmap.put(md.getColumnName(i), rs.getObject(i));
                    }
                }
                hashmap.put("answers", answersList);
                hashmap.put("metaData", countsMap);
                hashmap.put("usersInfo", userMap);
                list.add(hashmap);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally { //connection close
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //return null;//if connection error occur.
                }
            }
        }
        return list;
    }

    public List getQuestionsByUserID(int userID, int offset, int count){
        String GET_QUESTIONS_BY_USER_ID_QUERY = "SELECT `ID`, `question`, `creationTime` FROM `questions` WHERE `userID`= ? ORDER BY ID DESC LIMIT ?, ?";
        if (dbConnection()){
            return null;
        }
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = conn.prepareStatement(GET_QUESTIONS_BY_USER_ID_QUERY);
            ps.setInt(1, userID);
            ps.setInt(2, offset);
            ps.setInt(3, count);
            rs = ps.executeQuery();
            List list = new ArrayList();
            ResultSetMetaData md = rs.getMetaData();
            int column = md.getColumnCount();
            while (rs.next()){
                HashMap hashMap = new HashMap();
                for (int i=1; i<=column;++i){
                    hashMap.put(md.getColumnName(i), rs.getObject(i));
                }
                list.add(hashMap);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally { //connection close
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //return null;//if connection error occur.
                }
            }
        }
    }

    public List getCommentsByUserID(int userID, int offset, int count){
        String GET_COMMENTS_BY_USER_ID_QUERY = "SELECT `ID`, `userID`, `questionID`, `commentEmoji`, `commentText`, " +
                "`creationTime`, `likeCount`, `dislikeCount` FROM `comments` WHERE userID = ? ORDER BY ID LIMIT ?, ?";
        if (dbConnection()){
            return null;
        }
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = conn.prepareStatement(GET_COMMENTS_BY_USER_ID_QUERY);
            ps.setInt(1, userID);
            ps.setInt(2, offset);
            ps.setInt(3, count);
            rs = ps.executeQuery();

            List list = new ArrayList();
            ResultSetMetaData md = rs.getMetaData();
            int column = md.getColumnCount();
            while (rs.next()){
                HashMap hashMap = new HashMap();
                HashMap metaDataMap = new HashMap();
                for (int i=1; i<=column;++i){
                    String columnName = md.getColumnName(i);
                    if (columnName.contains("Count") || columnName.contains("Time")){
                        metaDataMap.put(columnName, rs.getInt(i));
                    } else{
                    hashMap.put(md.getColumnName(i), rs.getObject(i));
                    }
                }
                hashMap.put("metaData", metaDataMap);
                list.add(hashMap);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean setChoice(int userID, int questionID, int choice){//TODO: eger aynisi yoksa yaz yoksa ustune yaz//TODO:liste cevir error mesajlarini belirlemek icin
        String SET_CHOICE_QUERY = "INSERT INTO `answers`(`userID`, `questionID`, `choice`) VALUES ((SELECT users.ID from users WHERE users.ID = ?)," +
                "(SELECT questions.ID from questions WHERE questions.ID = ?), ?)";
        PreparedStatement ps;
        if (dbConnection()){
            return false;
        }

        int result = 0;
        try {
            ps = conn.prepareStatement(SET_CHOICE_QUERY);
            ps.setInt(1, userID);
            ps.setInt(2, questionID);
            ps.setInt(3, choice);
            result = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { //connection close
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    //return null;//if connection error occur.
                }
            }
        }
        if (result == 1){
            return true;
        }
        return false;
    }

    public int getChoice(int userID, int questionID){
        String GET_CHOICE_QUERY = "SELECT choice FROM `answers` WHERE userID = ? AND questionID = ?";
        PreparedStatement ps;
        ResultSet rs;
        try {
            ps = conn.prepareStatement(GET_CHOICE_QUERY);
            ps.setInt(1, userID);
            ps.setInt(2, questionID);
            rs = ps.executeQuery();
            if(rs.next())
                return rs.getInt(1);
            return -1;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

    }

    public boolean createComment(int userID, int questionID, String emoji, String comment){//TODO:liste cevir
        String CREATE_COMMENT_QUERY = "INSERT INTO `comments`(`userID`, `questionID`, `commentEmoji`, `commentText`)" +
                " VALUES ((SELECT users.ID FROM users WHERE users.ID = ?)," +
                "?, ?, ?)";
        String QUESTION_ID_CHECK_QUERY = "SELECT `questions`.`ID` FROM `questions` WHERE `questions`.`ID` = ?";
        if (dbConnection()){
            return false;
        }
        PreparedStatement ps;
        PreparedStatement ps2;
        ResultSet rs;
        try {
            ps = conn.prepareStatement(QUESTION_ID_CHECK_QUERY);
            ps.setInt(1, questionID);
            rs = ps.executeQuery();
            if (!rs.next())
                return false;

            ps2 = conn.prepareStatement(CREATE_COMMENT_QUERY);
            ps2.setInt(1, userID);
            ps2.setInt(2, questionID);
            ps2.setString(3, emoji);
            ps2.setString(4, comment);
            return ps2.executeUpdate() != 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally { //connection close
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List getCommentsForQuestion(int questionID, int offset, int count){
        //String COMMENTS_FOR_QUESTION = "SELECT `ID`, `userID`, `questionID`, `commentEmoji`, `commentText`," +
          //      " `creationTime`, `likeCount`, `dislikeCount` FROM `comments` WHERE `questionID` = ? LIMIT ?, ?";
        String COMMENTS_FOR_QUESTION = "SELECT comments.*, users.username, users.avatar FROM `comments` " +
                "INNER JOIN users ON users.ID = comments.userID WHERE comments.questionID = ? LIMIT ?, ?";
        if (dbConnection())
            return null;

        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = conn.prepareStatement(COMMENTS_FOR_QUESTION);
            ps.setInt(1, questionID);
            ps.setInt(2, offset);
            ps.setInt(3, count);
            rs = ps.executeQuery();

            ArrayList<Object> list = new ArrayList<>();
            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            while(rs.next()){
                HashMap<String,Object> metaDataMap = new HashMap<>();
                HashMap<String,Object> userInfoMap = new HashMap<>();
                HashMap<String,Object> hashmap = new HashMap<>(columns);
                for(int i=1; i<=columns ; ++i){
                    String columnName = md.getColumnName(i);
                    if (columnName.contains("Count") || columnName.contains("Time")){
                        metaDataMap.put(columnName, rs.getObject(i));
                    } else if (columnName.equals("username") || columnName.equals("avatar") || columnName.equals("userID")){
                        userInfoMap.put(columnName, rs.getObject(i));
                    } else{
                        hashmap.put(md.getColumnName(i), rs.getString(i));
                    }
                }
                hashmap.put("metaData", metaDataMap);
                hashmap.put("userInfo", userInfoMap);
                list.add(hashmap);
            }
            return list;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally { //connection close
            if(conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public HashMap<String,String> getUserByID(int userID){
        String GET_USER_BY_ID_QUERY = "SELECT `ID`, `username`, `avatar`, `creationTime`, `hasPermission` FROM `users` WHERE `ID` = ? ";
        if (dbConnection()){
            return null;
        }
        PreparedStatement ps;
        ResultSet rs;

        try {
            ps = conn.prepareStatement(GET_USER_BY_ID_QUERY);
            ps.setInt(1, userID);
            rs = ps.executeQuery();

            ResultSetMetaData md = rs.getMetaData();
            int columns = md.getColumnCount();
            HashMap<String, String> hashMap = new HashMap<>(columns);
            if (rs.next())
                for (int i=1; i<=columns; ++i){
                    hashMap.put(md.getColumnName(i), rs.getString(i));
                }
            return hashMap;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (conn != null){
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
