package com.retailmanager.rmpaydashboard.services.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.retailmanager.rmpaydashboard.models.Transactions;
import com.retailmanager.rmpaydashboard.services.DTO.TransactionDTO;

import jakarta.transaction.Transaction;





@Configuration
public class Mapper {
    
    
    /** 
     * @return ModelMapper
     */
    @Bean(name="mapperbase")
    public ModelMapper modelMapper(){
        ModelMapper objMapper= new ModelMapper();
        objMapper.getConfiguration().setPropertyCondition(context -> 
        !(context.getSource() instanceof org.hibernate.collection.spi.PersistentCollection));
        objMapper.typeMap(TransactionDTO.class, Transactions.class)
    .addMappings(mapper -> mapper.skip(Transactions::setSale));
        return objMapper;
    }
     
}
