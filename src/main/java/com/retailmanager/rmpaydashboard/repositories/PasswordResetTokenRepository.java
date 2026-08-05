package com.retailmanager.rmpaydashboard.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import com.retailmanager.rmpaydashboard.models.PasswordResetToken;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findOneByTokenHash(String tokenHash);

    @Transactional
    @Modifying
    @Query("DELETE FROM PasswordResetToken t WHERE t.user.userID = :userId")
    void deleteByUserId(Long userId);
}
