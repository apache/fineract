package org.apache.fineract.portfolio.clientaddress.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AddressRepository extends JpaRepository<Address,Long>,JpaSpecificationExecutor<Address>
{

}
