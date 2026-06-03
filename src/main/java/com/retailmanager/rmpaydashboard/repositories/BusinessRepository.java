package com.retailmanager.rmpaydashboard.repositories;

import com.retailmanager.rmpaydashboard.models.Business;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface BusinessRepository extends CrudRepository<Business,Long> {
    Optional<Business> findOneByMerchantId(String merchantId);
    @Modifying
    @Query("UPDATE Business u SET u.enable = :enable WHERE u.businessId = :businessId")
    void updateEnable(Long businessId, boolean enable);

    @Query("SELECT DISTINCT b FROM Business b WHERE b.user.name like :filter or b.user.username like :filter or b.user.email like :filter or b.user.phone like :filter or b.name like :filter or b.merchantId like :filter  or b.address.address1 like :filter or b.address.address2 like :filter or b.address.city like :filter or b.address.country like :filter")
    public Page<Business> findyAllClientsByFilter(Pageable pageable, String filter);

    @Query("SELECT DISTINCT B FROM Business B " )
    public Page<Business> findyAllClientsPageable(Pageable pageable);

    @Query("SELECT b FROM Business b WHERE b.registerDate BETWEEN :startDate AND :endDate")
    List<Business> findAllByRegistrations(Instant startDate, Instant endDate);
    
}
