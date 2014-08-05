/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.service;

import java.util.List;

import org.mifosplatform.mix.data.MixTaxonomyData;

public interface MixTaxonomyReadPlatformService {

    List<MixTaxonomyData> retrieveAll();

    MixTaxonomyData retrieveOne(Long id);
}
