package org.mifosplatform.mix.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MixTaxonomyMappingRepository extends JpaRepository<MixTaxonomyMapping, Long>, JpaSpecificationExecutor<MixTaxonomyMapping> {

}