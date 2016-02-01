/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.service;

import java.util.Collection;

import org.mifosplatform.organisation.provisioning.data.ProvisioningCategoryData;


public interface ProvisioningCategoryReadPlatformService {

    public Collection<ProvisioningCategoryData> retrieveAllProvisionCategories() ;
}
