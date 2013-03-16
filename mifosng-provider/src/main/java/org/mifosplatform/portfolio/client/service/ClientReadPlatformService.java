/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.service;

import java.util.Collection;

import org.mifosplatform.portfolio.client.data.ClientAccountSummaryCollectionData;
import org.mifosplatform.portfolio.client.data.ClientAccountSummaryData;
import org.mifosplatform.portfolio.client.data.ClientData;
import org.mifosplatform.portfolio.client.data.ClientLookup;

public interface ClientReadPlatformService {

    Collection<ClientData> retrieveAllIndividualClients(String extraCriteria);

    ClientData retrieveIndividualClient(Long clientId);

    ClientData retrieveNewClientDetails();

    Collection<ClientLookup> retrieveAllIndividualClientsForLookup(String extraCriteria);

    Collection<ClientLookup> retrieveAllIndividualClientsForLookupByOfficeId(Long officeId);

    ClientAccountSummaryCollectionData retrieveClientAccountDetails(Long clientId);

    Collection<ClientAccountSummaryData> retrieveClientLoanAccountsByLoanOfficerId(Long clientId, Long loanOfficerId);

    ClientData retrieveClientByIdentifier(Long identifierTypeId, String identifierKey);

}