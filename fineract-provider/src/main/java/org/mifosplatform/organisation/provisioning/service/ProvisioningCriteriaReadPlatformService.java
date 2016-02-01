/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.provisioning.service;

import java.util.Collection;

import org.mifosplatform.organisation.provisioning.data.ProvisioningCriteriaData;


public interface ProvisioningCriteriaReadPlatformService {

    public ProvisioningCriteriaData retrievePrivisiongCriteriaTemplate() ;
    
    public ProvisioningCriteriaData retrieveProvisioningCriteria(Long criteriaId) ;
    
    public Collection<ProvisioningCriteriaData> retrieveAllProvisioningCriterias() ;
    
    public ProvisioningCriteriaData retrievePrivisiongCriteriaTemplate(ProvisioningCriteriaData data) ;
}
