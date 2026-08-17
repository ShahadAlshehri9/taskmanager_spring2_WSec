package com.example.taskmanager.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import static javax.crypto.Cipher.SECRET_KEY;

@Component
//OncePerRequestFilter is a Spring base class that ensures doFilterInternal is called only once per request.
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
   /* jwtUtil  a utility component that handles JWT creation, validation, and username extraction.
    userDetailsService a custom implementation of Spring Security’s UserDetailsService that loads user data from database by username, and roles.*/
    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request//The incoming data packet sent by the client using this the filter will extract the header and the payload for the auth
                                    ,HttpServletResponse response //an outgoing data packet the server will send it back to the client once processing is finished
                                    ,FilterChain chain/* sequence of connected security checkpoints (filters) that a web request must pass through before it can reach the @RestController endpoints here it's used to send the for the next filter if any*/) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {//If the header is present and starts with "Bearer "
            String username = jwtUtil.validateAndGetUsername(header.substring(7));// the token is extracted, the token itself is everything after the prefix which is 7 char.
            //with .validateAndGetUsername() from the JwtUtil class returns a string username
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {//validation of username  and also check authentication If a valid username is obtained and no authentication exists yet
                UserDetails user = userDetailsService.loadUserByUsername(username);//Load the full user details
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
                /*A UsernamePasswordAuthenticationToken is created with The UserDetails object as the principal.
                null credentials (since we rely on the token, not a password).
                The user’s authorities (retrieved from user.getAuthorities()).*/
            }
        }
        chain.doFilter(request, response);//passes control to the next filter in the chain. If authentication was set, downstream security components like hasRole().
    }
}