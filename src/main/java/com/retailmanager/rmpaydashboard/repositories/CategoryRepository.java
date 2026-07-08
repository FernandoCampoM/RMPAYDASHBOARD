package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Business;
import com.retailmanager.rmpaydashboard.models.Category;


public interface CategoryRepository extends CrudRepository<Category, Long> {
    Optional<Category> findOneByName(String name);

    @Modifying
    @Query("UPDATE Category u SET u.enable = :enable WHERE u.categoryId = :categoryId")
    public int updateEnable(Long categoryId, boolean enable);
    
    public Optional<Category> findFirstByNameAndBusiness(String name, Business business);
    public List<Category> findByBusiness(Business business);
    @Query("SELECT c FROM Category c WHERE c.business.businessId = :businessId AND c.name = :name")
    public Optional<Category> findByNameAndBusinessId(String name, Long businessId);

    @Query(value = "select max(position) as position from [RMPAY].[dbo].[Category] where businessId=:businessId", nativeQuery = true)
    public Long maxPositionInCategory(Long businessId);
}
