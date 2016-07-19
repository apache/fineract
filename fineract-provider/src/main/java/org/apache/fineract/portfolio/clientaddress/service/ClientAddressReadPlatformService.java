package org.apache.fineract.portfolio.clientaddress.service;

import java.util.Collection;

import org.apache.fineract.portfolio.clientaddress.data.ClientAddressData;

public interface ClientAddressReadPlatformService {
    
    public Collection<ClientAddressData> retrieveClientAddrConfiguration(String entity);

}
