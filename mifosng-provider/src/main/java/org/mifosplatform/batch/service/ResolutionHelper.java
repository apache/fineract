package org.mifosplatform.batch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonModel;

/**
 * Provides methods to create dependency map among the various batchRequests.
 * It also provides method that takes care of dependency resolution among 
 * related requests.
 * 
 * @author Rishabh Shukla
 * @see BatchApiServiceImpl
 */
@Component
public class ResolutionHelper {
	
	private FromJsonHelper fromJsonHelper;
	
	@Autowired
	public ResolutionHelper(final FromJsonHelper fromJsonHelper) {
		this.fromJsonHelper = fromJsonHelper;
	}
	
	/**
	 * Returns a map containing requests that are divided in accordance of dependency relations 
	 * among them. Each different list is identified with a "Key" which is the "requestId" of 
	 * the request at topmost level in dependency hierarchy of that particular list.
	 * 
	 * @param requestList
	 * @return ConcurrentHashMap<Long, List<BatchRequest>>
	 */
	public ConcurrentHashMap<Long, ArrayList<BatchRequest>> getDependencyMap(final List<BatchRequest> requestList) {
		
		//Create a map with a '-1L' which will contain a list of independent requests
		final ConcurrentHashMap<Long, ArrayList<BatchRequest>> dependencyMap = new ConcurrentHashMap<>();
		
		dependencyMap.put(-1L, new ArrayList<BatchRequest>());	
		
		//For every request in the requestList 
		for(final BatchRequest br : requestList) {
			
			final Long reference = br.getReference();
			
			//If current request is dependent on some other request
			if(reference != null) {
				
				//Then find the request on which br is dependent on
				for(final BatchRequest parentRequest : requestList) {
					
					//If request on which br is dependent is found
					if(parentRequest.getRequestId().equals(reference)) {
						
						//Then start the process of adding it to one of the key in the dependencyMap						
						
						//If referenced request is in the list with key "-1L"
						if(dependencyMap.get(-1L).contains(parentRequest)) {
							
							//Then create a new list with key = reference
							final ArrayList<BatchRequest> newList = new ArrayList<>();
							newList.add(parentRequest);
							newList.add(br);
							
							//And add this list to the map
							dependencyMap.put(reference, newList);
							
							//Also remove parentRequest from the list at '-1L'
							final ArrayList<BatchRequest> listCopy = dependencyMap.get(-1L);
							
							listCopy.remove(parentRequest);
							
							dependencyMap.replace(-1L, listCopy);
						}
						//If referenced request is not in key '-1L'
						else {
							//Find the list in which it is present
							for(final ConcurrentHashMap.Entry<Long, ArrayList<BatchRequest>> entry: dependencyMap.entrySet()) {
								if(entry.getValue().contains(parentRequest)) {
									final ArrayList<BatchRequest> listCopy = entry.getValue();
									listCopy.add(br);
									
									//Add this request to this list
									dependencyMap.replace(entry.getKey(), listCopy);															
								}
							}
						}		
					}				
					
				}
			}
			//If request is independent then add it to '-1L'
			else {
				final ArrayList<BatchRequest> listCopy = dependencyMap.get(-1L);
				listCopy.add(br);
				
				dependencyMap.replace(-1L, listCopy);
			}
			 
		}
		
		return dependencyMap;
	}

	/**
	 * Returns a BatchRequest after dependency resolution. It takes a request and the
	 * response of the request it is dependent upon as its arguments and change the body
	 * or relativeUrl of the request according to parent Request.
	 * 
	 * @param request
	 * @param lastResponse
	 * @return BatchRequest
	 */
	public BatchRequest resoluteRequest(final BatchRequest request,final BatchResponse parentResponse) {
		
		//Create a duplicate request
		final BatchRequest br = request;
		
		final JsonModel responseJsonModel = JsonModel.model(parentResponse.getBody());
		
		//Gets the body from current Request as a JsonObject
		final JsonObject jsonRequestBody = this.fromJsonHelper.parse(request.getBody()).getAsJsonObject();
		
		JsonObject jsonResultBody = new JsonObject();
		
		//Iterate through each element in the requestBody to find dependent parameter
		for(Entry<String, JsonElement> element: jsonRequestBody.entrySet()) {
			
			String paramVal = element.getValue().getAsString();
			
			//If a dependent parameter is found
			if(paramVal.contains("$.")) {
				
				//Get the value of the parameter from parent response
				final String resParamValue = responseJsonModel.get(paramVal).toString();

				//Add the value after dependency resolution
				jsonResultBody.add(element.getKey(), this.fromJsonHelper.parse(resParamValue));
			}
			else {
				jsonResultBody.add(element.getKey(), element.getValue());
			}
		}		

		//Set the body after dependency resolution
		br.setBody(jsonResultBody.toString());		
		
		//Also check the relativeUrl for any dependency resolution
		String relativeUrl = request.getRelativeUrl(); 
		
		if(relativeUrl.contains("$.")) {
			
			final String parameter = relativeUrl.substring(relativeUrl.indexOf('/') + 1, relativeUrl.length());			
			
			//Get the value of the parameter from last response
			final String resParamValue = responseJsonModel.get(parameter).toString();

			relativeUrl = relativeUrl.replace(parameter, resParamValue);
						
			br.setRelativeUrl(relativeUrl);			
		}
		
		return br;
	}
	
}
