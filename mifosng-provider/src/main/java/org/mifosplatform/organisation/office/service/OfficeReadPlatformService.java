/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.office.service;

import java.util.Collection;

import org.mifosplatform.infrastructure.core.domain.MifosPlatformTenant;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.office.data.OfficeTransactionData;

public interface OfficeReadPlatformService {

    MifosPlatformTenant loadTenantById(String tenantIdentifier);

    Collection<OfficeData> retrieveAllOffices(boolean includeAllOffices);

    Collection<OfficeData> retrieveAllOfficesForDropdown();

    OfficeData retrieveOffice(Long officeId);

    OfficeData retrieveNewOfficeTemplate();

    Collection<OfficeData> retrieveAllowedParents(Long officeId);

    Collection<OfficeTransactionData> retrieveAllOfficeTransactions();

    OfficeTransactionData retrieveNewOfficeTransactionDetails();
}