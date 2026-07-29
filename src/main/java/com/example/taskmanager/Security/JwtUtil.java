package com.example.taskmanager.Security;

import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;


import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*This will generate a signed JWT for a given username (the subject).
and validate an incoming token and extract the username if the signature and expiration are valid. or null otherwise*/
@Component //makes this class a Spring-managed bean,
// so it can be auto‑wired into other components like authentication filters or controllers.
public class JwtUtil {

    private final SecretKey key;//A cryptographic key used to sign or verify JWT tokens.
    private final long expirationMs;//The token lifetime in milliseconds. after it will be invalid to use

    //@Value reads the properties app.jwt.secret and app.jwt.expiration-ms from application.properties
    //the secret will be converted into a secretKey using .hmacShaKeyFor() and the expirationMs same.
    public JwtUtil(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationMs = expirationMs;
    }
   /*This method creates a JWT with the following rules:
Subject = the username (the authenticated principal). //principal : represents the identity of the currently logged-in user (Who is this user?(UserDetails))
IssuedAt = current time.
Expiration = current time + expirationMs.
Signs the token using the key and the algorithm.
compact() produces the final URL-safe token string.*/
    public String generateToken(User user) {
        Map<String, Role> claims = new HashMap<>();

        //extracts the role  as enum from the user entity
        Role roleName = user.getRole() ;

        //add the single enum value to claims
        claims.put("role", roleName);
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    /*Parses the token using the same signing key automatically:
Verifies the signature - Checks the expiration time rejects if expired token - Validates the JWT structure
If parsing succeeds --> it returns the subject (username) from the payload.
If any exception occurs (JwtException), it catches and returns null.
This lets the caller decide how to handle invalid tokens (return 401 Unauthorized)
parsing a token means breaking down the compact string token into its core components (header, payload, and signature)
this step is achieved using parserBuilder() the parseClaimsJws(token) Verifies the signature  use secret key to ensure the token was not tampered with
also Validates claims checks standard constraints like expiration time (exp). If the token is expired or invalid, it throws an exception.
with parseClaimsJws we can use .getBody() which returns the payload which contains the username by using the getSubject() it will return the username
get subject to
     */
    public String validateAndGetUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}