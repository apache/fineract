/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.teller.data.TellerData;
import org.mifosplatform.organisation.teller.data.TellerJournalData;
import org.mifosplatform.organisation.teller.service.TellerManagementReadPlatformService;
import org.mifosplatform.organisation.teller.util.DateRange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("cashiersjournal")
@Component
@Scope("singleton")
public class TellerJournalApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<TellerData> jsonSerializer;
    private final TellerManagementReadPlatformService readPlatformService;

    @Autowired
    public TellerJournalApiResource(final PlatformSecurityContext securityContext,
            final DefaultToApiJsonSerializer<TellerData> jsonSerializer, final TellerManagementReadPlatformService readPlatformService) {
        super();
        this.securityContext = securityContext;
        this.jsonSerializer = jsonSerializer;
        this.readPlatformService = readPlatformService;
    }

    @GET
    @Consumes({ MediaType.TEXT_HTML, MediaType.APPLICATION_JSON })
    @Produces(MediaType.APPLICATION_JSON)
    public String getJournalData(@QueryParam("officeId") final Long officeId, @QueryParam("tellerId") final Long tellerId,
            @QueryParam("cashierId") final Long cashierId, @QueryParam("dateRange") final String dateRange) {
        final DateRange dateRangeHolder = DateRange.fromString(dateRange);

        final Collection<TellerJournalData> journals = this.readPlatformService.getJournals(officeId, tellerId, cashierId,
                dateRangeHolder.getStartDate(), dateRangeHolder.getEndDate());

        return this.jsonSerializer.serialize(journals);
    }
}
