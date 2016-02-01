/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.mix.api;

import java.sql.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.mix.data.XBRLData;
import org.mifosplatform.mix.service.XBRLBuilder;
import org.mifosplatform.mix.service.XBRLResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/mixreport")
@Component
@Scope("singleton")
public class MixReportApiResource {

    private final XBRLResultService xbrlResultService;
    private final XBRLBuilder xbrlBuilder;

    @Autowired
    public MixReportApiResource(final XBRLResultService xbrlResultService, final XBRLBuilder xbrlBuilder) {
        this.xbrlResultService = xbrlResultService;
        this.xbrlBuilder = xbrlBuilder;
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML })
    public String retrieveXBRLReport(@QueryParam("startDate") final Date startDate, @QueryParam("endDate") final Date endDate,
            @QueryParam("currency") final String currency) {

        final XBRLData data = this.xbrlResultService.getXBRLResult(startDate, endDate, currency);

        return this.xbrlBuilder.build(data);
    }
}