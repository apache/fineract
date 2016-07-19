package org.apache.fineract.portfolio.clientaddress.service;

import java.util.Collection;
import java.util.List;

import org.apache.fineract.portfolio.clientaddress.data.FieldConfigurationData;

public interface FieldConfigurationReadPlatformService 
{
    public Collection<FieldConfigurationData> retrieveFieldConfiguration(String entity);
    
   

    List<FieldConfigurationData> retrieveFieldConfigurationList(String entity);
}
