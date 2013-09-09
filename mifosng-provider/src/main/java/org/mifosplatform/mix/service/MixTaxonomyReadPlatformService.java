package org.mifosplatform.mix.service;

import java.util.List;

import org.mifosplatform.mix.data.MixTaxonomyData;

public interface MixTaxonomyReadPlatformService {

    List<MixTaxonomyData> retrieveAll();

    MixTaxonomyData retrieveOne(Long id);
}
