package com.elestir.Elestirorg;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.GsonBuilderUtils;
import org.springframework.web.bind.annotation.*;

import java.security.Key;
import java.util.*;
import java.util.regex.Pattern;

import io.jsonwebtoken.security.Keys;

@RestController
public class Controller {
    String secretkey= System.getenv("secret");
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

    private boolean validateToken(String token){
        if(token == null || !token.startsWith(prefix)){
            return false;
        }
        String jwtToken = token.replace(prefix, "");
        try{
            Claims claims = Jwts.parser()
                    .setSigningKey(key)
                    .parseClaimsJws(jwtToken)
                    .getBody();
            //
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
        if(username == null || password == null){
            return ResponseEntity.badRequest().body("missing header(s)");
            //return new ArrayList(Arrays.asList("missing header(s)"));
        }
        //db connection
        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.login(username,password);
        if(resultList.isEmpty()){
            return ResponseEntity.ok().body("email or password error");
            //return new ArrayList(Arrays.asList("email or password error"));
        }
        if (resultList.size() > 1){
            return ResponseEntity.ok().body("multiple account detected!");
        }
        return ResponseEntity.ok().body(resultList.get(0));
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST, produces = "application/json")
    public ResponseEntity<String> signup(@RequestHeader(value = "email", required = false) String email,
                                        @RequestHeader(value = "username", required = false) String username,
                                        @RequestHeader(value = "password",required = false) String password,
                                        @RequestHeader(value = "phonenumber",required = false) String phoneNumber){
        if(email==null || username == null || password == null || phoneNumber == null){
            return ResponseEntity.badRequest().body("missing header.");
        }
        StringController strController = new StringController();
        if(strController.isNumeric(phoneNumber)){
            return ResponseEntity.badRequest().body("phone number invalid.");
        }
        if(strController.isEmailValid(email)){
            return ResponseEntity.badRequest().body("email invalid.");
        }

        DatabaseConnection conn = new DatabaseConnection();
        List resultList = conn.signup(username, email, password, phoneNumber);
        return ResponseEntity.ok().body("cikis");
    }
}
