package com.grocery.userservice.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    
    @Value("${firebase.project-id:}")
    private String projectId;
    
    @Value("${firebase.service-account-key-path:}")
    private String serviceAccountKeyPath;
    
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (FirebaseApp.getApps().isEmpty() && !projectId.isEmpty() && !serviceAccountKeyPath.isEmpty()) {
            try {
                InputStream serviceAccount = new ClassPathResource(serviceAccountKeyPath).getInputStream();
                
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .setProjectId(projectId)
                        .build();
                
                return FirebaseApp.initializeApp(options);
            } catch (Exception e) {
                System.out.println("Firebase configuration not available, skipping Firebase initialization: " + e.getMessage());
                return null;
            }
        } else if (!FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.getInstance();
        }
        return null;
    }
    
    @Bean
    public FirebaseAuth firebaseAuth() throws IOException {
        FirebaseApp app = firebaseApp();
        if (app != null) {
            return FirebaseAuth.getInstance(app);
        }
        return null;
    }
}
