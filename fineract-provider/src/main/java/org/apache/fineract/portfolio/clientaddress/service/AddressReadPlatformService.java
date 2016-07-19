package org.apache.fineract.portfolio.clientaddress.service;

import java.util.Collection;

import org.apache.fineract.portfolio.clientaddress.data.AddressData;

public interface AddressReadPlatformService 
{
    public Collection<AddressData> retrieveAddressFields(long clientid);
    
    public Collection<AddressData> retrieveAllClientAddress(long clientid);
    
    public Collection<AddressData> retrieveAddressbyType(long clientid,long typeid);
    
    Collection<AddressData> retrieveAddressbyTypeAndStatus(long clientid,long typeid,String status);
    
    Collection<AddressData> retrieveAddressbyStatus(long clientid,String status);
    
    AddressData retrieveTemplate();
}
