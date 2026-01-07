package com.retailmanager.rmpaydashboard.config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;
import com.retailmanager.rmpaydashboard.security.JWTAthenticationFilter;
import com.retailmanager.rmpaydashboard.security.JWTAuthorizationFilter;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
//@AllArgsConstructor
@RequiredArgsConstructor
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private UserRepository usuarioRepository;
    @Autowired
    private TerminalRepository terminalRepository;
    private final JWTAuthorizationFilter jwtAuthorizationFilter;

    
   
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http,AuthenticationManager authenticationManager) throws Exception{

        JWTAthenticationFilter jwtAthenticationFilter= new JWTAthenticationFilter();
        jwtAthenticationFilter.setAuthenticationManager(authenticationManager);
        jwtAthenticationFilter.setFilterProcessesUrl("/login");
        jwtAthenticationFilter.setUsuarioRepository(usuarioRepository);
        jwtAthenticationFilter.setTerminalRepository(terminalRepository);

        return http.csrf(csrf->csrf.disable())
                .authorizeHttpRequests(authRequest->authRequest
                    .requestMatchers("/login").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/test/email").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/services/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/payment-methods/**").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/resellers/**").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/register").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/file").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/payAtTheTable/users").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/payAtTheTable/users/login").permitAll()
                    .requestMatchers(HttpMethod.GET,"/api/invoices/ATHM/checkTransaction/**").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/invoices/ATHM/confirmTransaction/**").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/invoices/ATHM/cancelTransaction/**").permitAll()
                    .requestMatchers("/users/password/**").hasAnyAuthority("ROLE_MANAGER_VIEW","ROLE_MANAGER")
                    .requestMatchers(HttpMethod.GET,"/api/**").hasAnyAuthority("ROLE_MANAGER_VIEW","ROLE_MANAGER","ROLE_USER","ROLE_USERRMPAYATTHETABLE")
                    .requestMatchers("/api/**").hasAnyAuthority("ROLE_MANAGER","ROLE_USER","ROLE_USERRMPAYATTHETABLE")
                    .anyRequest()
                    .authenticated())
                .sessionManagement(sesion->sesion.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                
                .addFilter(jwtAthenticationFilter)
                .addFilterBefore(jwtAuthorizationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                .accessDeniedHandler(accessDeniedHandler()))
                .build();
        
    }
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not allowed to access this resource");
            
        };
    }
    
}