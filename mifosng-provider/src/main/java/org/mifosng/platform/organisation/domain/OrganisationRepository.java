package org.mifosng.platform.organisation.domain;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {
    // no added behaviour
}
