/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.service;

import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.office.data.OfficeTransactionData;

public interface OfficeReadPlatformService {

    Collection<OfficeData> retrieveAllOffices();

    Collection<OfficeData> retrieveAllOfficesForDropdown();

    /*
     * Deprecated so cane eventually remove need for OfficeLookup type - so use
     * retrieveAllOfficesForDropdown instead.
     */
    @Deprecated
    Collection<OfficeLookup> retrieveAllOfficesForLookup();

    OfficeData retrieveOffice(Long officeId);

    OfficeData retrieveNewOfficeTemplate();

    Collection<OfficeData> retrieveAllowedParents(Long officeId);

    Collection<OfficeTransactionData> retrieveAllOfficeTransactions();

    OfficeTransactionData retrieveNewOfficeTransactionDetails();
}