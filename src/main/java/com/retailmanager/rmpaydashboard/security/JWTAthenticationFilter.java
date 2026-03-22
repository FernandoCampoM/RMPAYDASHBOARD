package com.retailmanager.rmpaydashboard.security;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.UserDisabled;
import com.retailmanager.rmpaydashboard.repositories.TerminalPayAtTableRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.repositories.UserPayAtTableRepository;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JWTAthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private UserRepository usuarioRepository;
    private TerminalRepository terminalRepository; 
    private TerminalPayAtTableRepository terminalPayAtTableRepository;
    private UserPayAtTableRepository userPayAtTableRepository;
    /** 
     * @param request
     * @param response
     * @return Authentication
     * @throws AuthenticationException
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        AuthCredentials authCredentials = new AuthCredentials();
        try {
            authCredentials= new ObjectMapper().readValue(request.getReader(), AuthCredentials.class);
           
            if(authCredentials.getTerminalId()!=null){
                if(authCredentials.getTerminalId().startsWith("RM")){
                    if(!terminalRepository.existsById(authCredentials.getTerminalId())){
                        throw new EntidadNoExisteException("El Terminal con ID "+authCredentials.getTerminalId()+" no existe en la Base de datos");
                    }else{
                        usuarioRepository.updateTempAuthId(authCredentials.getUsername(), authCredentials.getTerminalId());
                    }
                }else{
                    //Logica para PayAtTable
                }
                
            }
        } catch (IOException e) {
            
        }
        UsernamePasswordAuthenticationToken usernamePAT= new UsernamePasswordAuthenticationToken(
            authCredentials.getUsername(), authCredentials.getPassword(),Collections.emptyList());
        return getAuthenticationManager().authenticate(usernamePAT);
    }
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
        Authentication authResult) throws IOException, ServletException {
        
        UserDetailsImpl userDetails=(UserDetailsImpl) authResult.getPrincipal();
        if(userDetails.getUserObject().isEnable()==false){
            throw new UserDisabled("El USUARIO NO ESTA ACTIVO");
        }
        usuarioRepository.updateLastLogin(userDetails.getUserObject().getUserID(), Instant.now());
        String token=TokenUtils.createTokenWithClaims(userDetails.getUserObject(), terminalRepository);
        usuarioRepository.updateTempAuthId(userDetails.getUserObject().getUsername(), null);
        
        Token fullToken=new Token();
        fullToken.setAuthorization("Bearer "+token);
        response.addHeader("Authorization", "Bearer "+token);
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.println(fullToken.toJSON());
        response.getWriter().flush();

        super.successfulAuthentication(request, response, chain, authResult);
    }
    public void setUsuarioRepository(UserRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }
    public void setTerminalRepository(TerminalRepository terminalRepository) {
        this.terminalRepository = terminalRepository;
    }
    public void setTerminalPayAtTableRepository(TerminalPayAtTableRepository terminalPayAtTableRepository) {
        this.terminalPayAtTableRepository = terminalPayAtTableRepository;
    }
    public void setUserPayAtTableRepository(UserPayAtTableRepository userPayAtTableRepository) {
        this.userPayAtTableRepository = userPayAtTableRepository;
    }
}
