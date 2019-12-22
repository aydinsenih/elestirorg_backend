package com.elestir.Elestirorg;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.*;

import io.jsonwebtoken.security.Keys;

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
        resultMap.remove("ID");
        ResponseBodyController rbc = new ResponseBodyController(resultMap);
        //rbc.setReturnBody(resultMap);
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
//            rbc.put("username", username);
//            rbc.put("email" , email);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON(resultList.get(0).toString()));
    }

    @RequestMapping(value = "/isloggedin", method = RequestMethod.POST, consumes = "application/json")
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
//            rbc.put("username",claims.getSubject());
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
    }

    @RequestMapping(value = "/createquestion", method = RequestMethod.POST, consumes = "application/json")
    public ResponseEntity<String> createQuestion(@RequestBody(required = false) HashMap<String, Object> payload){
        if (payload == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("No data received."));
        }
        String token = (String) payload.get("token");
        String question = (String) payload.get("question");
        String category = (String) payload.get("category");
        HashMap<String, String> answers = (HashMap<String, String>) payload.get("answers");

        if(token==null || question == null || answers == null || category == null){
            return ResponseEntity.badRequest().body(getErrorResponseAsJSON("Missing data."));
        }

        Claims claims = validateToken(token);
        if (claims == null){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("token not valid."));
        }

        int answersSize = answers.size();
        if (!(5 >= answersSize && 2 <= answersSize)){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Answers must be minimum 2 and maximum 5"));
        }
        ResponseBodyController srbc = new ResponseBodyController();
        String sAnswer = srbc.serializeAnswers(answers);
        if (sAnswer == null){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Answers json error."));
        }

        String userID = claims.get("userID").toString();
        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.createQuestion(userID, question, sAnswer, category);
        if (resultList.get(0).toString().equals("success")){
            ResponseBodyController rbc = new ResponseBodyController();
            rbc.setStatus(rbc.SUCCESS);
            return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
        }
        return ResponseEntity.ok().body(getErrorResponseAsJSON("Unexpected error! Could not create question."));
    }

    @RequestMapping(value = "/getquestions", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<String> getQuestions(@RequestParam(value = "count",defaultValue = "5") int count,
                                              @RequestParam(value = "offset",defaultValue = "0") int offset){
        if(count - offset > 20){
            return ResponseEntity.ok().body(getErrorResponseAsJSON("Can not provide more than 20 questions."));
        }
        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.getQuestions(count, offset);
        ResponseBodyController rbc = new ResponseBodyController(resultList);
        rbc.setStatus(rbc.SUCCESS);
        return ResponseEntity.ok().body(rbc.getResponseBodyAsJson());
    }

}
