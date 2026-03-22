package com.retailmanager.rmpaydashboard.repositories;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.enums.Rol;
import com.retailmanager.rmpaydashboard.models.User;

public interface UserRepository extends  CrudRepository<User,Long>{
    Optional<User> findOneByUsername(String username);
    /**
     * Update the enable status for a specific user.
     *
     * @param  userID  the ID of the user to update
     * @param  enable  the new enable status
     */
    @Modifying
    @Query("UPDATE User u SET u.enable = :enable WHERE u.userID = :userID")
    void updateEnable(Long userID, boolean enable);
    /**
     * Update the last login date for a specific user.
     *
     * @param  userID    the ID of the user
     * @param  localDate the new last login date
     * @return           void
     */
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :localDate WHERE u.userID = :userID")
    void updateLastLogin(Long userID, Instant localDate);
    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.tempAuthId = :tempAuthId WHERE u.username = :username")
    void updateTempAuthId(String username, String tempAuthId);
    
    Optional<User> findOneByEmail(String username);
    @Query("SELECT u FROM User u WHERE u.enable = true and ELEMENT(u.business).additionalTerminals  > 0")
    public List<User> findActives();
    /**
     * Obtiene una lista de usuarios por la fecha de último inicio de sesión 
     * menor a una fecha dada.
     *
     * @param  date	fecha de último inicio de sesión dada
     * @return         listado de usuarios
     */
    //
    public List<User> findByLastLoginIsNullOrLastLoginLessThan(LocalDate date);

    @Query("SELECT DISTINCT u FROM User u WHERE u.rol=:rol and  u.name like :filter or u.username like :filter or u.email like :filter or u.phone like :filter or ELEMENT(u.business).name like :filter or ELEMENT(u.business).merchantId like :filter  or ELEMENT(u.business).address.address1 like :filter or ELEMENT(u.business).address.address2 like :filter or ELEMENT(u.business).address.city like :filter or ELEMENT(u.business).address.country like :filter")
    public Page<User> findyAllClientsByFilter(Pageable pageable, String filter, Rol rol);

    /*
     * Obtiene todos los usuarios con paginación
     */
    @Query("SELECT DISTINCT u FROM User u  WHERE u.rol=:rol" )
    public Page<User> findyAllClientsPageable(Pageable pageable, Rol rol);
    
    @Query("SELECT b FROM User b WHERE b.rol IN :rol order by b.username asc")
    List<User> findAllUsersManagers(List<Rol> rol);
}
