package com.elestir.Elestirorg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.*;
import java.util.regex.Pattern;

import io.jsonwebtoken.security.Keys;

@RestController
public class Controller {
    String secretkey = System.getenv("secret");
    private final Key key = Keys.hmacShaKeyFor(secretkey.getBytes());//Keys.secretKeyFor(SignatureAlgorithm.HS512);//https://stackoverflow.com/questions/40252903/static-secret-as-byte-key-or-string
    private final String prefix = "Bearer ";
    private final String issuer = "elestir.org";

    private String createToken(String username, String userID, String email){
        String jws = Jwts.builder()
                .claim("userID", userID)
                .claim("email", email)
                .setSubject(username)
                .setId(userID)
                .setIssuer(issuer)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        return prefix + jws;
    }

    private String validateToken(String token){
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

            return claims.toString();
        }
        catch (JwtException e){
            return null;
        }
    }


    @GetMapping
    public String welcome(){

        return "Elestirorg API";
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity userlogin(@RequestHeader(value = "accept-language", defaultValue = "en-us", required = false) String language,
                     @RequestHeader(value = "username", required = false) String username,
                     @RequestHeader(value = "password",required = false) String password){
        ReturnBodyController rbc = new ReturnBodyController();
        if(username == null || password == null){
            rbc.setStatus("Missing header(s).");
            return ResponseEntity.badRequest().body(rbc.getReturnBodyAsJson());
        }
        //db connection
        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.login(username,password);
        if (resultList == null){    //when sql connection error.
            rbc.setStatus("SQL connection error.");
            return ResponseEntity.ok().body(rbc.getReturnBodyAsJson());
        }
        if(resultList.isEmpty()){
            rbc.setStatus("Email or Password incorrect.");
            return ResponseEntity.ok().body(rbc.getReturnBodyAsJson());
            //return new ArrayList(Arrays.asList("email or password error"));
        }
        if (resultList.size() > 1){
            rbc.setStatus("Error! Multiple account detected!");
            return ResponseEntity.ok().body(rbc.getReturnBodyAsJson());
        }
        HashMap resultMap = (HashMap) resultList.get(0);
        String newToken;
        if(resultMap.get("token") == null || validateToken(resultMap.get("token").toString()) == null){
            newToken = createToken(resultMap.get("username").toString()
                    ,resultMap.get("ID").toString()
                    ,resultMap.get("email").toString());
            List updateTokenResult = conn.updateTokenForUser(resultMap.get("username").toString(),newToken);
            if (!updateTokenResult.get(0).toString().equals(username)){
                rbc.setStatus("SQL connection error. Could not update or create token.");
                return ResponseEntity.ok().body(rbc.getReturnBodyAsJson());
            }
            resultMap.put("token", newToken);
        }
        resultMap.remove("password");
        resultMap.remove("hasPermission");
        resultMap.remove("ID");

        rbc.setReturnBody(resultMap);
        rbc.setStatus("login success");
        return ResponseEntity.ok().body(rbc.getReturnBodyAsJson());//TODO : test required.
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> signup(@RequestHeader(value = "email", required = false) String email,
                                        @RequestHeader(value = "username", required = false) String username,
                                        @RequestHeader(value = "password",required = false) String password,
                                        @RequestHeader(value = "phonenumber",required = false) String phoneNumber){
        ReturnBodyController rbc = new ReturnBodyController();
        if(email==null || username == null || password == null || phoneNumber == null){
            rbc.setStatus("Missing header(s).");
            return ResponseEntity.badRequest().body(rbc.getReturnBodyAsJson());
        }
        StringController strController = new StringController();

        if(!strController.isNumeric(phoneNumber)){
            rbc.setStatus("Phone number invalid.");
            return ResponseEntity.badRequest().body(rbc.getReturnBodyAsJson());
        }
        if(!strController.isEmailValid(email)){
            rbc.setStatus("Email invalid.");
            return ResponseEntity.badRequest().body(rbc.getReturnBodyAsJson());
        }

        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.signup(username, email, password, phoneNumber);
        if (resultList == null){
            rbc.setStatus("SQL connection error. Sign-up could not complete.");
            return ResponseEntity.ok().body(rbc.getReturnBodyAsJson());
        }
        if (!resultList.get(0).toString().equals(username)){
            rbc.setStatus("Sign-up error.");
            return ResponseEntity.ok().body(rbc.getReturnBodyAsJson());
        }
        rbc.setStatus("sign-up success");
        rbc.put("username", username);
        rbc.put("email" , email);
        return ResponseEntity.ok().body(rbc.getReturnBodyAsJson());
    }
}
