package org.apache.fineract.portfolio.clientaddress.domain;

import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClientAddressRepository extends JpaRepository<ClientAddress,Long>,JpaSpecificationExecutor<ClientAddress>
{
    ClientAddress findByClientId(String clientId);
    ClientAddress findByClientIdAndAddressTypeAndIsActive(final long clientId, final CodeValue addressTypeId,final boolean isActive) ;
}
