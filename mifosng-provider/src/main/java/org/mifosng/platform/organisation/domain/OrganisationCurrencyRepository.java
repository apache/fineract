package org.mifosng.platform.organisation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OrganisationCurrencyRepository extends JpaRepository<OrganisationCurrency, Long>, JpaSpecificationExecutor<OrganisationCurrency> {
    // no added behaviour
}
