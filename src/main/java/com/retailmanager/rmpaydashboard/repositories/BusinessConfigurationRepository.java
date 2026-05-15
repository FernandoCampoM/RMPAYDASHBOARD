package com.retailmanager.rmpaydashboard.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.BusinessConfiguration;

public interface BusinessConfigurationRepository extends CrudRepository<BusinessConfiguration, Long> {

    @Query("SELECT b FROM BusinessConfiguration b WHERE b.business.businessId = :businessId")
    public List<BusinessConfiguration> findByBusinessId(Long businessId);

    @Query("SELECT b FROM BusinessConfiguration b WHERE b.configKey = :configKey AND b.business.businessId = :businessId")
    public BusinessConfiguration findByKey(String configKey, Long businessId);
    @Query("SELECT b FROM BusinessConfiguration b WHERE b.configKey like :configKey AND b.business.businessId = :businessId")
    public List<BusinessConfiguration> findByStartKey(String configKey, Long businessId);

    
}
