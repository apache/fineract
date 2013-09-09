package org.mifosplatform.xbrl.mapping.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TaxonomyMappingRepository extends JpaRepository<TaxonomyMapping, Long>, JpaSpecificationExecutor<TaxonomyMapping> {

}
