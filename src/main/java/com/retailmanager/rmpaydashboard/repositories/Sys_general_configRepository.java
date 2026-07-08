package com.retailmanager.rmpaydashboard.repositories;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.retailmanager.rmpaydashboard.models.Sys_general_config;


public interface Sys_general_configRepository extends CrudRepository<Sys_general_config,Long>{
    /**
     * Retrieves the Blackstone configuration data from the database.
     *
     * @return         	the Blackstone configuration data
     */
    @Query(value="SELECT " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.AppKey') AS AppKey, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.URL') AS url, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.AppType') AS AppType, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.MID') AS mid, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.CID') AS cid, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.Username') AS Username, " +
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.Password') AS Password,"+
                " (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.UrlForToken') AS UrlForToken,"+
                " (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.blackstone.UrlPaymentWithToken') AS UrlPaymentWithToken;", nativeQuery = true)
    public Object[] getBlackStoneConfig();
    @Query(value = "SELECT " + 
                    "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.athmovil.UrlPayment') AS UrlPayment, " + 
                                    "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.athmovil.UrlFindPayment') AS UrlFindPayment, " + 
                                                    "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.athmovil.UrlAuthorization') AS UrlAuthorization, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.athmovil.PublicToken') AS PublicToken, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.athmovil.UrlCancelPayment') AS UrlCancelPayment", nativeQuery = true)
    public Object[] getATHMovilConfig();
    /**
     * Retrieves email configuration data from the database.
     *
     * @return         	email configuration data containing key, emailFrom, emailTo, and emailCCO
     */
    @Query(value="SELECT " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.AppKey') AS appKey, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.emailFrom') AS emailFrom, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.emailTo') AS emailTo, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.emailCCO') AS emailCCO, " + 
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.RMPAYLogo') AS RMPAYLogo, "+
                "    (SELECT configvalue FROM RMPAY.dbo.Sys_general_config WHERE configlabel = 'config.email.RMLogo') AS RMLogo ", nativeQuery = true)
    public Object[] getEmailConfig();
}
