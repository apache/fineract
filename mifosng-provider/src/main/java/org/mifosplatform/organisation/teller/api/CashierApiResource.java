/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.api;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.teller.data.CashierData;
import org.mifosplatform.organisation.teller.service.TellerManagementReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Date;

@Path("cashiers")
@Component
@Scope("singleton")
public class CashierApiResource {

    private final PlatformSecurityContext securityContext;
    private final DefaultToApiJsonSerializer<CashierData> jsonSerializer;
    private final TellerManagementReadPlatformService readPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandWritePlatformService;

    @Autowired
    public CashierApiResource(PlatformSecurityContext securityContext, DefaultToApiJsonSerializer<CashierData> jsonSerializer,
                              TellerManagementReadPlatformService readPlatformService,
                              PortfolioCommandSourceWritePlatformService commandWritePlatformService) {
        this.securityContext = securityContext;
        this.jsonSerializer = jsonSerializer;
        this.readPlatformService = readPlatformService;
        this.commandWritePlatformService = commandWritePlatformService;
    }

    @GET
    @Consumes({MediaType.TEXT_HTML, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public String getCashierData(@QueryParam("officeId") final Long officeId, @QueryParam("tellerId") final Long tellerId,
                                 @QueryParam("staffId") final Long staffId, @QueryParam("date") final String date) {
        final DateTimeFormatter dateFormatter = ISODateTimeFormat.basicDate();

        final Date dateRestriction = (date != null ? dateFormatter.parseDateTime(date).toDate() : new Date());

        final Collection<CashierData> allCashiers = this.readPlatformService.getCashierData(officeId, tellerId, staffId, dateRestriction);

        return this.jsonSerializer.serialize(allCashiers);
    }
}
