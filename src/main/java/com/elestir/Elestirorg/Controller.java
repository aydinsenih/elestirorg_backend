package com.elestir.Elestirorg;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.*;

import io.jsonwebtoken.security.Keys;

@RestController
public class Controller {

    private final String secretkey = System.getenv("secret");
    private final Key key = Keys.hmacShaKeyFor(secretkey.getBytes());//Keys.secretKeyFor(SignatureAlgorithm.HS512);//https://stackoverflow.com/questions/40252903/static-secret-as-byte-key-or-string
    private final String prefix = "Bearer ";
    private final String issuer = "elestir.org";
    private final String userString = "user";
    private final String commentsString = "comments";
    private final String questionsString = "questions";


    private String createToken(String username, int userID, String email){
        String jws = Jwts.builder()
                .claim("userID", userID)
                .claim("email", email)
                .setSubject(username)
                .setId(Integer.toString(userID))
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        return prefix + jws;
    }

    private Claims validateToken(String token){
        if(token == null || !token.startsWith(prefix)){
            return null;
        }
        Claims claims;
        String jwtToken = token.replace(prefix, "");
        try{
            claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(jwtToken)
                    .getBody();
            return claims;
        }
        catch (JwtException e){
            return null;
        }
    }

    public String getErrorResponseAsJSON(String message){
        ResponseBodyController rbc = new ResponseBodyController();
        rbc.setStatus(rbc.FAILED);
        rbc.setMessage(message);
        return rbc.getResponseBodyAsJson();
    }


    @GetMapping
    public String welcome(){

        return "Elestirorg API v0.1";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json", consumes = "application/json")
    public ResponseEntity<String> userlogin(@RequestBody(required = false) HashMap<String, String> payload){
        if(payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }
        String username = payload.get("username");
        String password = payload.get("password");
        if(username == null || password == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing data."));
        }
        //db connection
        DatabaseConnection conn = new DatabaseConnection();
        HashMap<String,Object> resultMap = conn.login(username,password);
        if (resultMap == null){    //when sql connection error.
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("SQL connection error."));
        }
        if(resultMap.isEmpty()){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(getErrorResponseAsJSON("Email or Password incorrect."));
        }

        String newToken;
        if(resultMap.get("token") == null || validateToken(resultMap.get("token").toString()) == null){
            newToken = createToken(resultMap.get("username").toString()
                    ,Integer.parseInt(resultMap.get("ID").toString())
                    ,resultMap.get("email").toString());
            String updateTokenResult = conn.updateTokenForUser(resultMap.get("username").toString(),newToken);
            if (!updateTokenResult.equals(conn.SUCCESS)){
                return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Token could not updated."));
            }
            resultMap.put("token", newToken);
        }

        HashMap<String, Object> rHashMap = new HashMap<>();
        rHashMap.put("token", resultMap.get("token"));
        rHashMap.put("userID" , resultMap.get("ID"));
        ResponseBodyController rbc = new ResponseBodyController(userString, rHashMap);
        rbc.setStatus(rbc.SUCCESS);
        rbc.setMessage("login success");
        return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> signup(@RequestBody(required = false) HashMap<String, String> payload){
        if(payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }
        String email = payload.get("email");
        String username = payload.get("username");
        String password = payload.get("password");
        String phoneNumber = payload.get("phonenumber");

        if(email==null || username == null || password == null || phoneNumber == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing data."));
        }
        StringController strController = new StringController();

        if(!strController.isNumeric(phoneNumber)){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Phone number invalid."));
        }
        if(!strController.isEmailValid(email)){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Email invalid."));
        }

        DatabaseConnection conn = new DatabaseConnection();
        String result = conn.sign_up(username, email, password, phoneNumber);
        if (result.equals(conn.SUCCESS)){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            rbc.setMessage("sign-up success");
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON(result));
    }

    @RequestMapping(value = "/isloggedin", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> isLoggedin(@RequestBody(required = false) HashMap<String, String> payload){
        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }
        String token = payload.get("token");

        Claims claims = validateToken(token);

        if(claims == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("token not valid."));
        }
        else{
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
    }

    @RequestMapping(value = "/createquestion", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> createQuestion(@RequestBody(required = false) HashMap<String, Object> payload,
                                                 @RequestHeader(value = "AuthToken") String token){
        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }

        //String token = (token instanceof String) ? payload.get("token").toString() : null;
        String question = (payload.get("question") instanceof String) ? payload.get("question").toString() : null;
        String category = (payload.get("category") instanceof String) ? payload.get("category").toString() : null;
        Object answers = payload.get("answers");
        ArrayList<String> oAnswers = (answers instanceof ArrayList) ? (ArrayList<String>) answers : null;

        String answer1 = null;
        String answer2 = null;
        String answer3 = null;
        String answer4 = null;
        String answer5 = null;
        if(oAnswers != null) {
            int i = 0;
            for (String answer : oAnswers) {
                switch (i) {
                    case 0:
                        answer1 = answer.trim();
                        break;
                    case 1:
                        answer2 = answer.trim();
                        break;
                    case 2:
                        answer3 = answer.trim();
                        break;
                    case 3:
                        answer4 = answer.trim();
                        break;
                    case 4:
                        answer5 = answer.trim();
                        break;
                }
                i++;
            }
        }

//        if(token == null){
//            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing Token."));
//        }

        if(question == null || answer1 == null || answer2 == null || category == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing data."));
        }

        Claims claims = validateToken(token);
        if (claims == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("token not valid."));
        }

        int userID = Integer.parseInt(claims.get("userID").toString());
        DatabaseConnection conn = new DatabaseConnection();
        String result = conn.createQuestion(userID, question, category ,answer1 , answer2, answer3, answer4, answer5);
        if (result.equals(conn.SUCCESS)){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Unexpected error! Could not create question."));
    }

    @RequestMapping(value = "/getquestions", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> getQuestions(@RequestBody(required = false) HashMap<String,Object> payload,
                                               @RequestHeader(value = "AuthToken", required = false) String token){


        int count = 5;
        int offset = 0;
        int userID = 0;
        Claims claims;

        if (payload != null){
            if(token != null){
                claims = validateToken(token);
                if (claims != null){
                    userID = Integer.parseInt(claims.get("userID").toString());
                    if (payload.get("count") instanceof Integer){
                        count = Integer.parseInt(payload.get("count").toString());
                    }
                    if (payload.get("offset") instanceof Integer){
                        offset = Integer.parseInt(payload.get("offset").toString());
                    }
                }
            }
        }

        if(count > 20){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Cannot return more than 20 questions."));
        }

        DatabaseConnection conn = new DatabaseConnection();
        List<Object> resultList = conn.getQuestions(offset, count, userID);

        if (resultList != null) {
            ResponseBodyController rbc = new ResponseBodyController(questionsString, resultList);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/setchoice", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> setChoice(@RequestBody(required = false) HashMap<String, String> payload,
                                            @RequestHeader(value = "AuthToken") String token){

        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }

        //String token = payload.get("token");
        String sQuestionID = payload.get("questionID");
        String sChoice = payload.get("choice");

        if (token == null || sQuestionID == null || sChoice == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing data."));
        }

        Claims claims = validateToken(token);
        if(claims == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Token not valid."));
        }

        int userID;
        int questionID;
        int choice;
        try {
            userID = Integer.parseInt(claims.get("userID").toString());
            questionID = Integer.parseInt(payload.get("questionID"));
            choice = Integer.parseInt(payload.get("choice"));
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid data."));
        }

        if (choice > 5 || choice < 1){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid integer data."));
        }

        DatabaseConnection conn = new DatabaseConnection();

        if (conn.setChoice(userID, questionID, choice).equals(conn.SUCCESS)){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Unexpected error! Could not set choice."));

    }

    @RequestMapping(value = "/createcomment", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> createComment(@RequestBody(required = false) HashMap<String,String> payload,
                                                @RequestHeader(value = "AuthToken") String token){
        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }

        //String token = payload.get("token");
        String qID = payload.get("questionID");
        String cText = payload.get("comment");
        String cEmoji = payload.get("emoji");
        int userID;
        int questionID;

        if (token == null || qID == null || cText == null || cEmoji == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing data."));
        }

        Claims claims = validateToken(token);
        if(claims == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Token not valid."));
        }
        try {
            userID = Integer.parseInt(claims.get("userID").toString());
            questionID = Integer.parseInt(qID);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid data."));
        }


        DatabaseConnection conn = new DatabaseConnection();
        if (conn.createComment(userID, questionID, cEmoji, cText).equals(conn.SUCCESS)){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("SQL error!"));

    }

    @RequestMapping(value = "/getcomments/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> getComments(@PathVariable(value = "id", required = false)String sQuestionID,
                                              @RequestParam(value = "offset", defaultValue = "0")String sOffset,
                                              @RequestParam(value = "count", defaultValue = "5")String sCount){
        int questionID,count,offset;
        try {
            questionID = Integer.parseInt(sQuestionID);
            offset = Integer.parseInt(sOffset);
            count = Integer.parseInt(sCount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid data."));
        }

        DatabaseConnection conn = new DatabaseConnection();
        List<Object> result = conn.getCommentsForQuestion(questionID, offset, count);
        if (result != null){
            ResponseBodyController rbc = new ResponseBodyController(commentsString, result);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/user/{id}", produces = "application/json", consumes = "application/json")
    public ResponseEntity<String> getUserByID(@PathVariable(value = "id", required = false)String sUserID,
                                              @RequestHeader(value = "AuthToken", required = false) String token){
        int userID;
        try {
            userID = Integer.parseInt(sUserID);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid data."));
        }
        DatabaseConnection conn = new DatabaseConnection();
        HashMap<String, Object> resultList = conn.getUserByID(userID);
        if (resultList != null){
            if (!resultList.isEmpty()){
                Claims claims = validateToken(token);
                if (claims != null){
                    if (Integer.parseInt(claims.get("userID").toString()) == userID){
                        ResponseBodyController rbc = new ResponseBodyController(userString, resultList);
                        rbc.setStatus(rbc.SUCCESS);
                        return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
                    }
                }
                resultList.remove("email");
                resultList.remove("phoneNumber");
                ResponseBodyController rbc = new ResponseBodyController(userString, resultList);
                rbc.setStatus(rbc.SUCCESS);
                return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
            }
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("User not found."));
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/getquestionsbyuserid/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getQuestionsByUserID(@PathVariable(value = "id", required = false)String sUserID,
                                                       @RequestParam(value = "offset", defaultValue = "0")String sOffset,
                                                       @RequestParam(value = "count", defaultValue = "5")String sCount){
        int userID,count,offset;
        try {
            userID = Integer.parseInt(sUserID);
            offset = Integer.parseInt(sOffset);
            count = Integer.parseInt(sCount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid data."));
        }

        if (count > 20)
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Cannot return more than 20 questions."));
        DatabaseConnection conn = new DatabaseConnection();
        List<Object> resultList = conn.getQuestionsByUserID(userID, offset, count);
        if (resultList != null){
            ResponseBodyController rbc = new ResponseBodyController(questionsString, resultList);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/getcommentsbyuserid/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getCommentsByUserID(@PathVariable(value = "id", required = false)String sUserID,
                                                      @RequestParam(value = "offset", defaultValue = "0")String sOffset,
                                                      @RequestParam(value = "count", defaultValue = "5")String sCount){
        int userID,count,offset;
        try {
            userID = Integer.parseInt(sUserID);
            offset = Integer.parseInt(sOffset);
            count = Integer.parseInt(sCount);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid data."));
        }

        if (count > 20)
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Cannot return more than 20 comments."));
        DatabaseConnection conn = new DatabaseConnection();
        List<Object> resultList = conn.getCommentsByUserID(userID, offset, count);
        if (resultList != null){
            ResponseBodyController rbc = new ResponseBodyController(commentsString, resultList);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/question/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> getQuestionByQuestionID(@PathVariable(value = "id", required = false)String sQuestionID,
                                                          @RequestBody(required = false) HashMap<String, String> payload,
                                                          @RequestHeader(value = "AuthToken", required = false) String token){
        int questionID;
        try {
            questionID = Integer.parseInt(sQuestionID);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid data."));
        }
        HashMap<String, Object> hashMap;
        if (token != null){
            Claims claims = validateToken(token);
            if (claims != null){
                DatabaseConnection conn = new DatabaseConnection();
                hashMap = conn.getQuestionByQuestionID(questionID, Integer.parseInt(claims.get("userID").toString()));
            }
            else return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid token."));
        } else {
            DatabaseConnection conn = new DatabaseConnection();
            hashMap = conn.getQuestionByQuestionID(questionID, 0);
        }
        if (hashMap != null){
            ResponseBodyController rbc = new ResponseBodyController(questionsString, hashMap);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.badRequest().body(getErrorResponseAsJSON("SQL error!"));
    }

}
