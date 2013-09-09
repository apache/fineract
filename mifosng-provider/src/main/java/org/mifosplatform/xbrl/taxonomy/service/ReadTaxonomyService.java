package org.mifosplatform.xbrl.taxonomy.service;

import java.util.List;

import org.mifosplatform.xbrl.taxonomy.data.TaxonomyData;

public interface ReadTaxonomyService {

    List<TaxonomyData> retrieveAllTaxonomy();

    TaxonomyData retrieveTaxonomyById(Long id);
}
