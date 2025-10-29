package com.retailmanager.rmpaydashboard.security;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.retailmanager.rmpaydashboard.enums.Rol;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.UserDisabled;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.User;
import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_Terminal;
import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_User;
import com.retailmanager.rmpaydashboard.repositories.TerminalPayAtTableRepository;
import com.retailmanager.rmpaydashboard.repositories.TerminalRepository;
import com.retailmanager.rmpaydashboard.repositories.UserPayAtTableRepository;
import com.retailmanager.rmpaydashboard.repositories.UserRepository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.transaction.Transactional;

public class TokenUtils {
    private final static String ACCESS_TOKEN_SECRET="gybPPZLk9ShzIv6V1Zl/xGL0MjAUOY+u327FmRrt7ZI=";
    private final static Long ACCESS_TOKEN_VALIDITY_SECONDS=2_592_000L; 
    private final static Long ACCESS_TOKEN_VALIDITY_SECONDS_PAY_AT_TABLE=2_592_000L; //
    

    
    
    /** 
     * @param user
     * @return String
     */
    public static String createToken(User user){
        long expirationTime=ACCESS_TOKEN_VALIDITY_SECONDS *1_000;
        Date expirationDate=new Date(System.currentTimeMillis() + expirationTime);
        Map<String, Object> extra= new HashMap<>();
        extra.put("nombre", user.getName());
        extra.put("roles", user.getRol().toString());
        
        return Jwts.builder()
                    .setSubject(user.getUsername())
                    .setExpiration(expirationDate)
                    .addClaims(extra)
                    .signWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes()))
                    .compact();
    }
    public static String createTokenWithClaims(User user, TerminalRepository terminalRepository){
        long expirationTime=ACCESS_TOKEN_VALIDITY_SECONDS *1_000;
        Date expirationDate=new Date(System.currentTimeMillis() + expirationTime);
        Map<String, Object> extra= new HashMap<>();
        extra.put("nombre", user.getName());
        extra.put("roles", user.getRol().toString());
        if(user.getTempAuthId()!=null){
            extra.put("terminalId", user.getTempAuthId());
            Terminal terminal=terminalRepository.findById(user.getTempAuthId()).orElseThrow(()->new EntidadNoExisteException("Terminal no encontrada con id: "+user.getTempAuthId()));
            extra.put("terminalExpirationDate", terminal.getExpirationDate().toString());
        }
        
        
        return Jwts.builder()
                    .setSubject(user.getUsername())
                    .setExpiration(expirationDate)
                    .addClaims(extra)
                    .signWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes()))
                    .compact();
    }
    public static String createTokenWithClaims(RMPayAtTheTable_User user, String serialNumber){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        long expirationTime=ACCESS_TOKEN_VALIDITY_SECONDS_PAY_AT_TABLE*1_000;
        Date expirationDate=new Date(System.currentTimeMillis() + expirationTime);
        System.out.println("Expiration date: "+expirationDate.toString());
        Map<String, Object> extra= new HashMap<>();
        extra.put("nombre", user.getBusinessName());
        extra.put("roles", Rol.ROLE_USERRMPAYATTHETABLE.toString());
        extra.put("serialNumber", serialNumber);
        extra.put("expirationDate", formatter.format(expirationDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()));
        
        
        
        return Jwts.builder()
                    .setSubject(user.getUsername())
                    .setExpiration(expirationDate)
                    .addClaims(extra)
                    .signWith(Keys.hmacShaKeyFor(ACCESS_TOKEN_SECRET.getBytes()))
                    .compact();
    }

    @Transactional
    public static UsernamePasswordAuthenticationToken getAuthentication(String token, UserRepository usuarioRepository, UserPayAtTableRepository userPayAtTableRepository,TerminalPayAtTableRepository terminalPayAtTableRepository){
        String terminalIdLong=null;
        
        try {
            String serialNumber=null;
            System.out.println("TOKEN EN UTILS: "+token);
            Claims claims = Jwts.parserBuilder()
                            .setSigningKey(ACCESS_TOKEN_SECRET.getBytes())
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
            String username=claims.getSubject();
            System.out.println("username en UTILS: "+username);
            System.out.println("claims: "+claims.keySet());
            if(claims.keySet().contains("terminalId")){
                terminalIdLong=claims.get("terminalId", String.class);
            }
            if(claims.keySet().contains("serialNumber")){
                serialNumber=claims.get("serialNumber", String.class);
            }
            //
            if(serialNumber!=null){
                RMPayAtTheTable_User usuario= userPayAtTableRepository.findByUsername(username).orElseThrow(()-> new UsernameNotFoundException("El usuario con user "+username+" no existe"));
                
                RMPayAtTheTable_Terminal terminal= terminalPayAtTableRepository.findBySerialNumber(serialNumber).orElse(null);
                
                UserDetailsImpl objUser=new UserDetailsImpl(usuario);
                
                if(terminal==null){
                    throw new EntidadNoExisteException("El terminal con serial number "+serialNumber+" no existe");
                }else{

                    if(terminal.getUser().getUserId().equals(usuario.getUserId())==false){
                        System.out.println("usrid terminal: "+terminal.getUser().getUserId()+
                        "Name: "+terminal.getUser().getName()+" usrid user: "+usuario.getUserId()+
                        "name: "+usuario.getName());
                        throw new EntidadNoExisteException("El terminal con serial number "+serialNumber+" no pertenece al usuario "+username);
                    }
                    if(!terminal.getActive()){
                        throw new UserDisabled("El terminal con serial number "+serialNumber+" esta desactivado");
                    }
                }
                return new UsernamePasswordAuthenticationToken(username,null,objUser.getAuthorities());
            }else{
                User usuario= usuarioRepository.findOneByUsername(username).orElseThrow(()-> new UsernameNotFoundException("El usuario con user "+username+" no existe"));
                UserDetailsImpl objUser=new UserDetailsImpl(usuario);
                if(terminalIdLong!=null){usuario.setTempAuthId(terminalIdLong);}
                usuarioRepository.updateLastLogin(usuario.getUserID(),LocalDate.now());
                return new UsernamePasswordAuthenticationToken(username,null,objUser.getAuthorities());
            }
        } catch (JwtException  e) {
            System.out.println("JWT EXCEPTION: "+e.getMessage());
            return null;
        }catch (Exception  e) {
            System.out.println("EXCEPTION: "+e.getMessage());
            return null;
        }
        
    }
    /**
     * Retrieves the terminal ID associated with the given token, or null if the token is invalid or does not contain a terminal ID.
     * 
     * @param token the token to parse
     * @return the terminal ID associated with the token, or null if the token is invalid or does not contain a terminal ID
     */
    public static String getTerminalId(String token){
        try {
            Claims claims = Jwts.parserBuilder()
                            .setSigningKey(ACCESS_TOKEN_SECRET.getBytes())
                            .build()
                            .parseClaimsJws(token)
                            .getBody();
        String username=claims.getSubject();
        String terminalId=claims.get("terminalId", String.class);
        if(terminalId!=null){
            return terminalId;
        }
    } catch (JwtException  e) {
        return null;
    }catch (Exception  e) {
        return null;
        
    
    }
    return null;

    }
}
