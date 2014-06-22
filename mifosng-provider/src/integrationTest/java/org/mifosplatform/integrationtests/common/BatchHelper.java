package org.mifosplatform.integrationtests.common;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Helper class for {@link org.mifosplatform.integrationtests.BatchApiTest}.
 * It takes care of creation of {@code BatchRequest} list and posting this
 * list to the server.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.integrationtests.BatchApiTest
 */
public class BatchHelper {

	private static final String BATCH_API_URL = "/mifosng-provider/api/v1/batches?tenantIdentifier=default";
	private static final String BATCH_API_URL_EXT = BATCH_API_URL + "&enclosingTransaction=true";
	
	private BatchHelper() {
		super();
	}
	
	/**
	 * Returns a JSON String for a list of {@code BatchRequest}s
	 * 
	 * @param batchRequests
	 * @return JSON String of BatchRequest
	 */
	public static String toJsonString(final List<BatchRequest> batchRequests) {
		return new Gson().toJson(batchRequests);
	}

	/**
	 * Returns the converted string response into JSON.
	 * 
	 * @param json
	 * @return List<BatchResponse>
	 */
    private static List<BatchResponse> fromJsonString(final String json) {
        return new Gson().fromJson(json, new TypeToken<List<BatchResponse>>(){}.getType());
    }
    

	/**
	 * Returns a list of BatchResponse with query paramater enclosing transaction set to
	 *  false by posting the jsonified BatchRequest to the server.
	 * 
	 * @param requestSpec
	 * @param responseSpec
	 * @param jsonifiedBatchRequests
	 * @return a list of BatchResponse
	 */
	public static List<BatchResponse> postBatchRequestsWithoutEnclosingTransaction(final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final String jsonifiedBatchRequests) {
		final String response = Utils.performServerPost(requestSpec, responseSpec, BATCH_API_URL, jsonifiedBatchRequests, null);
	    return BatchHelper.fromJsonString(response);
	}
	
	/**
	 * Returns a list of BatchResponse with query paramater enclosing transaction set to
	 * true by posting the jsonified BatchRequest to the server.
	 * 
	 * @param requestSpec
	 * @param responseSpec
	 * @param jsonifiedBatchRequests
	 * @return a list of BatchResponse
	 */
	public static List<BatchResponse> postBatchRequestsWithEnclosingTransaction(final RequestSpecification requestSpec,
			final ResponseSpecification responseSpec, final String jsonifiedBatchRequests) {
		final String response = Utils.performServerPost(requestSpec, responseSpec, BATCH_API_URL_EXT, jsonifiedBatchRequests, null);
	    return BatchHelper.fromJsonString(response);
	}
	
	/**
	 * Creates a new Client Request as one of the request in Batch.
	 * 
	 * @param reqId
	 * @param externalId
	 * @return BatchRequest
	 */
	public static BatchRequest createClientRequest(final Long reqId, final String externalId) {
		
		final BatchRequest br = new BatchRequest();
		br.setRequestId(reqId);
		br.setRelativeUrl("clients");
		br.setMethod("POST");
		
		final String extId;
		if(externalId.equals("")) {
			extId = "ext" + String.valueOf((10000 * Math.random())) + String.valueOf((10000 * Math.random()));
		}
		else {
			extId = externalId;
		}
		
		final String body = "{ \"officeId\": 1, \"firstname\": \"Petra\", \"lastname\": \"Yton\"," +  
		"\"externalId\": " + extId + ",  \"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en\"," + 
		"\"active\": false, \"activationDate\": \"04 March 2009\", \"submittedOnDate\": \"04 March 2009\"}";
		
		br.setBody(body);
		
		return br;
	}
	
    /**
     * Returns a BatchResponse based on the given BatchRequest, by posting the request
     * to the server.
     * 
     * @param BatchRequest
     * @return List<BatchResponse>
     */
    public static List<BatchResponse> createRequest(final RequestSpecification requestSpec,
    		final ResponseSpecification responseSpec,final BatchRequest br) {
    	
    	final List<BatchRequest> batchRequests = new ArrayList<>();
		batchRequests.add(br);
		
		final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
		final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(requestSpec,
				responseSpec, jsonifiedRequest);
		
		//Verifies that the response result is there
		Assert.assertNotNull(response);
		Assert.assertTrue(response.size() > 0);
		
		return response;
    }
}