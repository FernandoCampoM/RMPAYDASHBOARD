package com.retailmanager.rmpaydashboard.services.services.RMPayAtTheTable.UserServices;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadNoExisteException;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.EntidadYaExisteException;
import com.retailmanager.rmpaydashboard.models.Terminal;
import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_Terminal;
import com.retailmanager.rmpaydashboard.models.rmpayAtTheTable.RMPayAtTheTable_User;
import com.retailmanager.rmpaydashboard.repositories.TerminalPayAtTableRepository;
import com.retailmanager.rmpaydashboard.repositories.UserPayAtTableRepository;
import com.retailmanager.rmpaydashboard.security.TokenUtils;
import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_TerminalDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.RMPayAtTheTable_UserDTO;
import com.retailmanager.rmpaydashboard.services.DTO.RMPayAtTheTable.UserAuthDTO;

import org.hibernate.Hibernate;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserPayAtTableService implements IUserPayAtTableService {
    @Autowired
    private UserPayAtTableRepository userRepository;
    @Autowired
    private TerminalPayAtTableRepository terminalRepository;

    @Autowired
    @Qualifier("mapperbase")
    private ModelMapper mapper;

    @Override
    public ResponseEntity<RMPayAtTheTable_UserDTO> createUser(RMPayAtTheTable_UserDTO userDTO) {
        RMPayAtTheTable_User user = mapper.map(userDTO, RMPayAtTheTable_User.class);
        user.setUnencryptedPassword(user.getPassword());
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        
        if(userRepository.findByMerchantId(user.getMerchantId()).isPresent()) 
            throw new EntidadYaExisteException("El merchantId " + user.getMerchantId() + " ya existe en la Base de datos");
        if(userRepository.findByUsername(userDTO.getUsername()).isPresent()) 
            throw new EntidadYaExisteException("Ya existe un usuario con El username " + user.getUsername() + " en la Base de datos");
            RMPayAtTheTable_User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.map(savedUser, RMPayAtTheTable_UserDTO.class));
    }

    @Override
    public ResponseEntity<RMPayAtTheTable_UserDTO> updateUser(Long userId, RMPayAtTheTable_UserDTO userDTO) {
        RMPayAtTheTable_User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntidadNoExisteException("El usuario con ID " + userId + " no existe en la Base de datos"));
        if(userDTO.getMerchantId().compareTo(user.getMerchantId())!=0)
            if(userRepository.findByMerchantId(userDTO.getMerchantId()).isPresent()) 
                throw new EntidadYaExisteException("El merchantId " + userDTO.getMerchantId() + " ya existe en la Base de datos");
        if(userDTO.getUsername().compareTo(user.getUsername())!=0)
            if(userRepository.findByUsername(userDTO.getUsername()).isPresent())
                throw new EntidadYaExisteException("Ya existe un usuario con El username " + userDTO.getUsername() + " en la Base de datos");
                user.setAddress(userDTO.getAddress());
        user.setBusinessName(userDTO.getBusinessName());
        user.setPhone(userDTO.getPhone());
        user.setMerchantId(userDTO.getMerchantId());
        user.setName(userDTO.getName());
        user.setTokenATHMovil(userDTO.getTokenATHMovil());
        RMPayAtTheTable_User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(mapper.map(updatedUser, RMPayAtTheTable_UserDTO.class));
    }

    @Override
    public ResponseEntity<?> deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntidadNoExisteException("El usuario con ID " + userId + " no existe en la Base de datos");
        }
        try {
            userRepository.deleteById(userId);
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
        
        
    }

    @Override
    public ResponseEntity<RMPayAtTheTable_UserDTO> getUserById(Long userId) {
        RMPayAtTheTable_User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntidadNoExisteException("El usuario con ID " + userId + " no existe en la Base de datos"));
        RMPayAtTheTable_UserDTO userDTO = mapper.map(user, RMPayAtTheTable_UserDTO.class);
        
        
        userDTO.setTerminals(user.getTerminals().stream().map(terminal -> terminal.toDTO()).collect(Collectors.toList()));
                return ResponseEntity.ok(userDTO);
    }

    @Override
    public ResponseEntity<?> getAllUsers(Pageable pageable, String filter) {
        Page<RMPayAtTheTable_User> users = null;
        if (filter != null) {
            filter = "%" + filter + "%";
            users = userRepository.findyAllUsersByFilter(pageable, filter);
        } else {
            users = userRepository.findyAllUsersByFilter(pageable);
        }
            
        List<RMPayAtTheTable_UserDTO> userDTOs = users.getContent().stream()
        .map(user -> {
            RMPayAtTheTable_UserDTO userDTO = mapper.map(user, RMPayAtTheTable_UserDTO.class);
            // Mapear los terminales del usuario a DTOs
            userDTO.setTerminals(
                user.getTerminals().stream()
                    .map(RMPayAtTheTable_Terminal::toDTO) // Convertir cada terminal a DTO
                    .collect(Collectors.toList())
            );
            return userDTO;
        })
        .collect(Collectors.toList());
        Page<RMPayAtTheTable_UserDTO> userDTOPage = new PageImpl<>(userDTOs, pageable, users.getTotalElements());
        return ResponseEntity.ok(userDTOPage); 
    }

    @Override
    public ResponseEntity<RMPayAtTheTable_UserDTO> getUserByUsername(String username) {
        RMPayAtTheTable_User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntidadNoExisteException("El usuario con username " + username + " no existe en la Base de datos"));
                RMPayAtTheTable_UserDTO userDTO = mapper.map(user, RMPayAtTheTable_UserDTO.class);
        
        
                userDTO.setTerminals(user.getTerminals().stream().map(terminal -> terminal.toDTO()).collect(Collectors.toList()));
                        return ResponseEntity.ok(userDTO);}

    @Override
    public ResponseEntity<RMPayAtTheTable_UserDTO> getUserByMerchantId(String merchantId) {
        RMPayAtTheTable_User user= userRepository.findByMerchantId(merchantId)
        .orElseThrow(() -> new EntidadNoExisteException("El usuario con merchantId " + merchantId + " no existe en la Base de datos"));
        
        RMPayAtTheTable_UserDTO userDTO = mapper.map(user, RMPayAtTheTable_UserDTO.class);
        
        
        userDTO.setTerminals(user.getTerminals().stream().map(terminal -> terminal.toDTO()).collect(Collectors.toList()));
                return ResponseEntity.ok(userDTO);}

    @Override
    public ResponseEntity<?> authenticaton(UserAuthDTO userAuthDTO) {
        System.out.println("Inicio de sesion: " + userAuthDTO.getUsername());
        RMPayAtTheTable_User user = userRepository.findByUsername(userAuthDTO.getUsername()).orElse(null);
        HashMap<String, String> map = new HashMap<String, String>();
        if (user == null) {
            map.clear();
            map.put("message", "Usuario o contraseña incorrectos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
        }
                
        if(!new BCryptPasswordEncoder().matches(userAuthDTO.getPassword(), user.getPassword())){
            map.clear();
            map.put("message", "Usuario o contraseña incorrectos");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(map);
        }

        List<RMPayAtTheTable_Terminal> terminals = user.getTerminals();
        Optional<RMPayAtTheTable_Terminal> terminalOpt = terminals.stream().filter(t -> t.getSerialNumber().equals(userAuthDTO.getSerialNumber())).findFirst();
        if (terminalOpt.isPresent()) {
            RMPayAtTheTable_Terminal terminal = terminalOpt.get();
            if(terminal.getUser().getUserId()!=user.getUserId()){
                map.clear();
                map.put("message", "El terminal con serial number " + userAuthDTO.getSerialNumber() + " pertenece a otro usuario");
                System.out.println("El terminal con serial number " + userAuthDTO.getSerialNumber() + " pertenece a otro usuario");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(map);
            }
            if (!terminal.getActive()) {
                map.clear();
                map.put("message", "El terminal con serial number " + userAuthDTO.getSerialNumber() + " está desactivado");
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(map);
            }
        } else {

            RMPayAtTheTable_Terminal newTerminal = terminalRepository.findBySerialNumber(userAuthDTO.getSerialNumber()).orElse(null);
            if (newTerminal != null) {
                map.clear();
                map.put("message", "El terminal con serial number " + userAuthDTO.getSerialNumber() + " ya existe en la Base de datos y pertenece a otro usuario");
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(map);
            }
            newTerminal = new RMPayAtTheTable_Terminal();
            newTerminal.setSerialNumber(userAuthDTO.getSerialNumber());
            newTerminal.setActive(true);
            newTerminal.setUser(user);
            newTerminal.setRegistrationDate(LocalDate.now());
            terminalRepository.save(newTerminal);
        }

        String token=TokenUtils.createTokenWithClaims(user, userAuthDTO.getSerialNumber());
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("Authorization", "Bearer "+token);

        return new ResponseEntity<>(map2, HttpStatus.OK);
    }
    @Override
public ResponseEntity<?> passwordChange(Long userId, String newPassword) {
    RMPayAtTheTable_User user = userRepository.findById(userId)
            .orElseThrow(() -> new EntidadNoExisteException("El usuario con ID " + userId + " no existe en la Base de datos"));
    
    // Encriptar la nueva contraseña
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    String encodedPassword = passwordEncoder.encode(newPassword);

    user.setUnencryptedPassword(newPassword);
    user.setPassword(encodedPassword);
    userRepository.save(user);
    HashMap<String, String> map = new HashMap<String, String>();
    map.put("message", "Contraseña actualizada correctamente");
    return ResponseEntity.ok(map);
}

}
