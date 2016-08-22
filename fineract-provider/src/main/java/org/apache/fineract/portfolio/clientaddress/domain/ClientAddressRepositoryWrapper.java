package org.apache.fineract.portfolio.clientaddress.domain;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.portfolio.clientaddress.exception.AddressNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClientAddressRepositoryWrapper 
{
private ClientAddressRepository clientAddressRepository;

@Autowired
public ClientAddressRepositoryWrapper(final ClientAddressRepository clientAddressRepository)
{
    this.clientAddressRepository=clientAddressRepository; 
}


public ClientAddress findOneByClientIdAndAddressTypeAndIsActive(final long clientId, final CodeValue addressType,final boolean isActive) 
{
    final ClientAddress clientAddress = this.clientAddressRepository.findByClientIdAndAddressTypeAndIsActive(clientId, addressType,isActive);
 //   if (clientAddress == null) { throw new AddressNotFoundException(clientId, addressType); }
    return clientAddress;
}


}
