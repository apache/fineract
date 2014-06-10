package org.mifosplatform.integrationtests;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.integrationtests.common.BatchHelper;
import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.builder.RequestSpecBuilder;
import com.jayway.restassured.builder.ResponseSpecBuilder;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

/**
 * Test class for {@link org.mifosplatform.batch.command.CommandStrategyProvider}.
 * This tests the response provided by commandStrategy by injecting it with
 * a {@code BatchRequest}.
 * 
 * @author RishabhShukla
 * 
 * @see org.mifosplatform.integrationtests.common.BatchHelper
 * @see org.mifosplatform.batch.domain.BatchRequest
 */
public class BatchApiTest {

	private ResponseSpecification responseSpec;
	private RequestSpecification requestSpec;
	
	public BatchApiTest() {
		super();
	}
	
	/**
	 * Sets up the essential settings for the TEST like contentType, expectedStatusCode.
	 * It uses the '@Before' annotation provided by jUnit.
	 */
	@Before
	public void setup() {
		
		Utils.initializeRESTAssured();
		this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
		this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
		this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
	}
	
	//Tests for the unimplemented command Strategies by returning 501 status code
	@Test
	public void shouldReturnStatusNotImplementedUnknownCommand() {

		final BatchRequest br = new BatchRequest();
		br.setRequestId(4711L);
		br.setRelativeUrl("/nirvana");
		br.setMethod("POST");

		final List<BatchResponse> response = BatchHelper.createRequest(this.requestSpec, this.responseSpec, br);

		//Verify that only 501 is returned as the status code
		for(BatchResponse resp : response) {
			Assert.assertEquals("Verifying Status code 501",(long) 501,(long) resp.getStatusCode());
		}
	}
	
	//Tests for the successful response for a createClient request from createClientCommand
	@Test
	public void shouldReturnOkStatusForCreateClientCommand() {
		
		final BatchRequest br = BatchHelper.createClientRequest(4712L, "");
		
		final List<BatchResponse> response = BatchHelper.createRequest(this.requestSpec, this.responseSpec, br);
		
		//Verify that a 200 response is returned as the status code
		for(BatchResponse resp : response) {
			Assert.assertEquals("Verifying Status code 200",(long) 200,(long) resp.getStatusCode());
		}
	}
	
	//Tests for an erroneous response with statusCode 501 if transaction fails
	@Test
	public void shouldRollBackAllTransactionsOnFailure() {
		
		//Create first client
		final BatchRequest br1 = BatchHelper.createClientRequest(4713L, "TestExtId1");
		
		//Create second client
		final BatchRequest br2 = BatchHelper.createClientRequest(4714L, "TestExtId2");   
		
		//Create third client, having same externalID as second client, hence cause of error
		final BatchRequest br3 = BatchHelper.createClientRequest(4715L, "TestExtId2"); 
		
		final List<BatchRequest> batchRequests = new ArrayList<>();
		
		batchRequests.add(br1);
		batchRequests.add(br2);
		batchRequests.add(br3);
		
		final String jsonifiedRequest = BatchHelper.toJsonString(batchRequests);
		final List<BatchResponse> response = BatchHelper.postBatchRequestsWithEnclosingTransaction(this.requestSpec,
				this.responseSpec, jsonifiedRequest);
		
		Assert.assertEquals(response.size(), 1);
		Assert.assertEquals("Verifying Status code 400",(long) 400,(long) response.get(0).getStatusCode());		
	}
}