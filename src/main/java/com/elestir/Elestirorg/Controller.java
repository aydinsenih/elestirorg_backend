package com.elestir.Elestirorg;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jnr.ffi.annotations.In;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.*;

import io.jsonwebtoken.security.Keys;

import javax.xml.crypto.Data;

@RestController
public class Controller {
    String secretkey = System.getenv("secret");
    private final Key key = Keys.hmacShaKeyFor(secretkey.getBytes());//Keys.secretKeyFor(SignatureAlgorithm.HS512);//https://stackoverflow.com/questions/40252903/static-secret-as-byte-key-or-string
    private final String prefix = "Bearer ";
    private final String issuer = "elestir.org";

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
        List resultList = conn.login(username,password);
        if (resultList == null){    //when sql connection error.
            return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL connection error."));
        }
        if(resultList.isEmpty()){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Email or Password incorrect."));
            //return new ArrayList(Arrays.asList("email or password error"));
        }
        if (resultList.size() > 1){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Error! Multiple account detected!"));
        }
        HashMap resultMap = (HashMap) resultList.get(0);
        String newToken;
        if(resultMap.get("token") == null || validateToken(resultMap.get("token").toString()) == null){
            newToken = createToken(resultMap.get("username").toString()
                    ,Integer.parseInt(resultMap.get("ID").toString())
                    ,resultMap.get("email").toString());
            List updateTokenResult = conn.updateTokenForUser(resultMap.get("username").toString(),newToken);
            if (!updateTokenResult.get(0).toString().equals(username)){
                return ResponseEntity.ok().body(getErrorResponseAsJSON(updateTokenResult.get(0).toString()));
            }
            resultMap.put("token", newToken);
        }
        //resultMap.remove("ID");
        ResponseBodyController rbc = new ResponseBodyController(resultMap);
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
        List resultList = conn.signup(username, email, password, phoneNumber);
        if (resultList == null){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL connection error. Sign-up could not complete."));
        }
        if (resultList.get(0).toString().equals(username)){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            rbc.setMessage("sign-up success");
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON(resultList.get(0).toString()));
    }

    @RequestMapping(value = "/isloggedin", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> isLoggedin(@RequestBody(required = false) HashMap<String, String> payload){
        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }
        String token = payload.get("token");

        Claims claims = validateToken(token);

        if(claims == null){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("token not valid."));
        }
        else{
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
    }

    @RequestMapping(value = "/createquestion", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> createQuestion(@RequestBody(required = false) HashMap<String, Object> payload){
        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }

        String token = (payload.get("token") instanceof String) ? payload.get("token").toString() : null;
        String question = (payload.get("question") instanceof String) ? payload.get("question").toString() : null;
        String category = (payload.get("category") instanceof String) ? payload.get("category").toString() : null;
        Object oAnswers = payload.get("answers");

        String answer1 = null;
        String answer2 = null;
        String answer3 = null;
        String answer4 = null;
        String answer5 = null;
        if(oAnswers instanceof ArrayList) {
            ArrayList answers = (ArrayList) oAnswers;
            int i = 0;
            for (Object answer : answers) {
                switch (i) {
                    case 0:
                        answer1 = answer.toString().trim();
                        break;
                    case 1:
                        answer2 = answer.toString().trim();
                        break;
                    case 2:
                        answer3 = answer.toString().trim();
                        break;
                    case 3:
                        answer4 = answer.toString().trim();
                        break;
                    case 4:
                        answer5 = answer.toString().trim();
                        break;
                }
                i++;
            }
        }

        if(token == null || question == null || answer1 == null || answer2 == null || category == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing data."));
        }

        Claims claims = validateToken(token);
        if (claims == null){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("token not valid."));
        }

        int userID = Integer.parseInt(claims.get("userID").toString());
        DatabaseConnection conn = new DatabaseConnection();
        List<String> resultList = conn.createQuestion(userID, question, category ,answer1 , answer2, answer3, answer4, answer5);
        if (resultList.get(0).equals("success")){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("Unexpected error! Could not create question."));
    }

    @RequestMapping(value = "/getquestions", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> getQuestions(@RequestBody(required = false) HashMap<String,Object> payload){

        String token;
        int count = 5;
        int offset = 0;
        int userID = 0;
        Claims claims;

        if (payload != null){
            token = (payload.get("token") instanceof String) ? payload.get("token").toString() : null;
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
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Cannot return more than 20 questions."));
        }

        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.getQuestions(offset, count, userID);

        if (resultList != null) {
            ResponseBodyController rbc = new ResponseBodyController(resultList);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/setchoice", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> setChoice(@RequestBody(required = false) HashMap<String, String> payload){

        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }

        String token = payload.get("token");
        String sQuestionID = payload.get("questionID");
        String sChoice = payload.get("choice");

        if (token == null || sQuestionID == null || sChoice == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing data."));
        }

        Claims claims = validateToken(token);
        if(claims == null){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Token not valid."));
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
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Invalid data."));
        }

        if (choice > 5 || choice < 1){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid integer data."));
        }

        DatabaseConnection conn = new DatabaseConnection();

        if (conn.setChoice(userID, questionID, choice)){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("Unexpected error! Could not set choice."));

    }

    @RequestMapping(value = "/createcomment", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> createComment(@RequestBody(required = false) HashMap<String,String> payload){
        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }

        String token = payload.get("token");
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
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Token not valid."));
        }
        try {
            userID = Integer.parseInt(claims.get("userID").toString());
            questionID = Integer.parseInt(qID);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Invalid data."));
        }


        DatabaseConnection conn = new DatabaseConnection();
        if (conn.createComment(userID, questionID, cEmoji, cText)){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL error!"));

    }

    @RequestMapping(value = "/getcomments/{id}", method = RequestMethod.POST, consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> getComments(@PathVariable(value = "id")int questionID,
                                              @RequestParam(value = "offset")int offset,
                                              @RequestParam(value = "count")int count){
//        if (payload == null){
//            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
//        }
//        if (payload.get("questionID") == null){
//            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Invalid data."));
//        }
//        int questionID = payload.get("questionID");
//        int offset = (payload.get("offset") != null) ? payload.get("offset") : 0;
//        int count = (payload.get("count") != null) ? payload.get("count") : 5;

        DatabaseConnection conn = new DatabaseConnection();
        List result = conn.getCommentsForQuestion(questionID, offset, count);
        if (result != null){
            ResponseBodyController rbc = new ResponseBodyController(result);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getUserByID(@PathVariable(value = "id")int userID){

        DatabaseConnection conn = new DatabaseConnection();
        HashMap<String, String> resultList = conn.getUserByID(userID);
        if (resultList != null){
            ResponseBodyController rbc = new ResponseBodyController(resultList);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }

        return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/getquestionsbyuserid/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getQuestionsByUserID(@PathVariable("id")int userID,
                                                       @RequestParam(value = "offset", defaultValue = "0")int offset,
                                                       @RequestParam(value = "count", defaultValue = "5")int count){
        if (count > 20)
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Cannot return more than 20 questions."));
        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.getQuestionsByUserID(userID, offset, count);
        if (resultList != null){
            ResponseBodyController rbc = new ResponseBodyController(resultList);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/getcommentsbyuserid/{id}", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getCommentsByUserID(@PathVariable("id")int userID,
                                                      @RequestParam(value = "offset", defaultValue = "0")int offset,
                                                      @RequestParam(value = "count", defaultValue = "5")int count){
        if (count > 20)
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Cannot return more than 20 comments."));
        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.getCommentsByUserID(userID, offset, count);
        if (resultList != null){
            ResponseBodyController rbc = new ResponseBodyController(resultList);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL error!"));
    }

    @RequestMapping(value = "/question/{id}", consumes = "application/json", produces = "application/json")
    public ResponseEntity<String> getQuestionByQuestionID (@PathVariable("id")int questionID,
                                                           @RequestBody(required = false)HashMap<String, String> payload){
        HashMap hashMap;
        if (payload != null && payload.get("token") != null){
            Claims claims = validateToken(payload.get("token"));
            if (claims != null){
                DatabaseConnection conn = new DatabaseConnection();
                hashMap = conn.getQuestionByQuestionID(questionID, Integer.parseInt(claims.get("userID").toString()));
            }
            else return ResponseEntity.ok().body(getErrorResponseAsJSON("Invalid token."));
        } else {
            DatabaseConnection conn = new DatabaseConnection();
            hashMap = conn.getQuestionByQuestionID(questionID, 0);
        }
        if (hashMap != null){
            ResponseBodyController rbc = new ResponseBodyController(hashMap);
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("SQL error!"));
    }

}
