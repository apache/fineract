package org.apache.fineract.CreditCheck.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrganisationCreditBureauRepository extends JpaRepository<OrganisationCreditBureau, Long>, JpaSpecificationExecutor<OrganisationCreditBureau> 
{

}
