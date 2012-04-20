package org.mifosng.ui.extradata;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mifosng.data.EntityIdentifier;
import org.mifosng.data.ErrorResponse;
import org.mifosng.data.ExtraDatasets;
import org.mifosng.data.reports.GenericResultset;
import org.mifosng.ui.CommonRestOperations;
import org.mifosng.ui.loanproduct.ClientValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class ExtraDataController {
	
	private final CommonRestOperations commonRestOperations;

	@Autowired
	public ExtraDataController(final CommonRestOperations commonRestOperations) {
		this.commonRestOperations = commonRestOperations;
	}
	
    @ExceptionHandler(AccessDeniedException.class)
	public String accessDeniedException() {
		return "unAuthorizedAction";
	}
	
	@ExceptionHandler(ClientValidationException.class)
	public @ResponseBody Collection<ErrorResponse> validationException(ClientValidationException ex, HttpServletResponse response) {
		
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
		response.setContentType("application/json");
		
		return ex.getValidationErrors();
	}
		

	@RequestMapping(consumes="application/json", produces="application/json", value = "/extradata/datasets/{datasetType}", method = RequestMethod.GET)
	public @ResponseBody ExtraDatasets retrieveExtraDatasets(@PathVariable("datasetType") String datasetType) {

		return this.commonRestOperations.retrieveExtraDatasets(datasetType);
	}
	
	@RequestMapping(consumes="application/json", produces="application/json", value = "/extradata/{datasetType}/{datasetName}/{datasetPKValue}", method = RequestMethod.GET)
	public @ResponseBody GenericResultset viewExtraData(@PathVariable("datasetType") String datasetType, @PathVariable("datasetName") String datasetName, @PathVariable("datasetPKValue") String datasetPKValue) {

		return this.commonRestOperations.retrieveExtraData(datasetType, datasetName, datasetPKValue);
	}
	
	@RequestMapping(produces="application/json", value = "/extradata/{datasetType}/{datasetName}/{datasetPKValue}", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public @ResponseBody EntityIdentifier saveExtraData(HttpServletRequest request, @PathVariable("datasetType") String datasetType,@PathVariable("datasetName") String datasetName, @PathVariable("datasetPKValue") String datasetPKValue)  {
		return this.commonRestOperations.saveExtraData(datasetType, datasetName, datasetPKValue, request.getParameterMap());
	}
}