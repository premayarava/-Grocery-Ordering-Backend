package com.grocery.userservice.config;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired(required = false)
    private FirebaseAuth firebaseAuth;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().permitAll() // Allow all requests when Firebase is not configured
            );
        
        if (firebaseAuth != null) {
            http.addFilterBefore(new FirebaseAuthenticationFilter(firebaseAuth), UsernamePasswordAuthenticationFilter.class);
        }
        
        return http.build();
    }
    
    public static class FirebaseAuthenticationFilter extends org.springframework.web.filter.OncePerRequestFilter {
        
        private final FirebaseAuth firebaseAuth;
        
        public FirebaseAuthenticationFilter(FirebaseAuth firebaseAuth) {
            this.firebaseAuth = firebaseAuth;
        }
        
        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                      FilterChain filterChain) throws ServletException, IOException {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                    request.setAttribute("firebaseUid", decodedToken.getUid());
                    request.setAttribute("firebaseEmail", decodedToken.getEmail());
                } catch (Exception e) {
                    // Token is invalid, but we'll let the request continue
                    // The controller can handle authentication as needed
                }
            }
            
            filterChain.doFilter(request, response);
        }
    }
}
