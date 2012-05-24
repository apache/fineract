package org.mifosng.platform.api;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ser.FilterProvider;
import org.codehaus.jackson.map.ser.impl.SimpleBeanPropertyFilter;
import org.codehaus.jackson.map.ser.impl.SimpleFilterProvider;
import org.joda.time.LocalDate;
import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.OfficeData;
import org.mifosng.data.OfficeList;
import org.mifosng.data.command.OfficeCommand;
import org.mifosng.platform.ReadExtraDataAndReportingServiceImpl;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.organisation.service.OfficeReadPlatformService;
import org.mifosng.platform.organisation.service.OfficeWritePlatformService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/v1/offices")
@Component
@Scope("singleton")
public class OfficeApiResource {

	private final static Logger logger = LoggerFactory
			.getLogger(OfficeApiResource.class);
    @Autowired
	private OfficeReadPlatformService readPlatformService;

	@Autowired
	private OfficeWritePlatformService writePlatformService;
	
	@Autowired
	private ApiDataConversionService apiDataConversionService;
/*
    @GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON})
	public Response retrieveOffices() {
		
		Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices();

		return Response.ok().entity(new OfficeList(offices)).build();
	}*/
    @GET
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON})
	public String retrieveOffices() {
		
		Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices();

	    Set<String> filterProperties = new HashSet<String>();
		/*StringTokenizer st = new StringTokenizer(fields, ",");
	    while (st.hasMoreTokens()) {
	        filterProperties.add(st.nextToken());
	    }*/
	    filterProperties.add("name");
	    
	    ObjectMapper mapper = new ObjectMapper();
	    FilterProvider filters = new SimpleFilterProvider().addFilter("myFilter",
	                SimpleBeanPropertyFilter.filterOutAllExcept(filterProperties));

	    try {
	        String json = mapper.filteredWriter(filters).writeValueAsString(offices);
	        logger.info("office list json: " + json);
	        return json;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return e.getMessage();
	    }
	    
		//return Response.ok().entity(new OfficeList(offices)).build();
	}
    
    @GET
    @Path("template")
   	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
    @Produces({MediaType.APPLICATION_JSON})
   	public Response retrieveOfficeTemplate() {

    	OfficeData newOfficeTemplate = this.readPlatformService.retrieveNewOfficeTemplate();

   		return Response.ok().entity(newOfficeTemplate).build();
   	}
    
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response createOffice(final OfficeCommand command) {
		
		LocalDate openingDate = apiDataConversionService.convertFrom(command.getOpeningDateFormatted(), "openingDateFormatted", command.getDateFormat());
		command.setOpeningDate(openingDate);
		
		Long officeId = this.writePlatformService.createOffice(command);

		return Response.ok().entity(new EntityIdentifier(officeId)).build();
	}

	@GET
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response retreiveOffice(@PathParam("officeId") final Long officeId) {
		
		OfficeData office = this.readPlatformService.retrieveOffice(officeId);

		return Response.ok().entity(office).build();
    }

	@PUT
	@Path("{officeId}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON})
	public Response updateOffice(@PathParam("officeId") final Long officeId, final OfficeCommand command) {

		LocalDate openingDate = apiDataConversionService.convertFrom(command.getOpeningDateFormatted(), "openingDateFormatted", command.getDateFormat());
		command.setOpeningDate(openingDate);
		command.setId(officeId);

		Long entityId = this.writePlatformService.updateOffice(command);

		return Response.ok().entity(new EntityIdentifier(entityId)).build();
	}
}