package org.mifosplatform.xbrl.report.api;

import java.sql.Date;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.xbrl.report.service.XBRLBuilder;
import org.mifosplatform.xbrl.report.service.XBRLResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/mixreport")
@Component
@Scope("singleton")
public class XBRLReportApiResource {

    private final PlatformSecurityContext context;
    private final XBRLResultService xbrlResultService;
    @Autowired
    private XBRLBuilder xbrlBuilder;

    @Autowired
    public XBRLReportApiResource(final PlatformSecurityContext context, final XBRLResultService xbrlResultService) {
        this.context = context;
        this.xbrlResultService = xbrlResultService;
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML })
    public String retrieveXBRLReport(@QueryParam("startDate") final Date startDate, @QueryParam("endDate") final Date endDate,
            @QueryParam("currency") final String currency) {

        context.authenticatedUser();

        String xbrl = xbrlBuilder.build(this.xbrlResultService.getXBRLResult(startDate, endDate, currency));
        return xbrl;
    }

}
