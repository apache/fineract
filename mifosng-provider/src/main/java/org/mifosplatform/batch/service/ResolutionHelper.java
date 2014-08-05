/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jayway.jsonpath.JsonModel;

/**
 * Provides methods to create dependency map among the various batchRequests. It
 * also provides method that takes care of dependency resolution among related
 * requests.
 * 
 * @author Rishabh Shukla
 * @see BatchApiServiceImpl
 */
@Component
public class ResolutionHelper {

    /**
     * Provides a Node like object for the request tree.
     * 
     * @author Rishabh shukla
     * 
     */
    public class BatchRequestNode {

        private BatchRequest request;
        private final List<BatchRequestNode> childRequests = new ArrayList<>();

        public BatchRequestNode() {
            super();
        }

        public BatchRequest getRequest() {
            return this.request;
        }

        public void setRequest(BatchRequest request) {
            this.request = request;
        }

        public List<BatchRequestNode> getChildRequests() {
            return this.childRequests;
        }

        public void addChildRequest(final BatchRequestNode batchRequest) {
            this.childRequests.add(batchRequest);
        }

    }

    private FromJsonHelper fromJsonHelper;

    @Autowired
    public ResolutionHelper(final FromJsonHelper fromJsonHelper) {
        this.fromJsonHelper = fromJsonHelper;
    }

    /**
     * Returns a map containing requests that are divided in accordance of
     * dependency relations among them. Each different list is identified with a
     * "Key" which is the "requestId" of the request at topmost level in
     * dependency hierarchy of that particular list.
     * 
     * @param batchRequests
     * @return List<ArrayList<BatchRequestNode>>
     */
    public List<BatchRequestNode> getDependingRequests(final List<BatchRequest> batchRequests) {
        final List<BatchRequestNode> rootRequests = new ArrayList<>();

        for (BatchRequest batchRequest : batchRequests) {
            if (batchRequest.getReference() == null) {
                final BatchRequestNode node = new BatchRequestNode();
                node.setRequest(batchRequest);
                rootRequests.add(node);
            } else {
                this.addDependingRequest(batchRequest, rootRequests);
            }
        }

        return rootRequests;
    }

    private void addDependingRequest(final BatchRequest batchRequest, final List<BatchRequestNode> parentRequests) {
        for (BatchRequestNode batchRequestNode : parentRequests) {
            if (batchRequestNode.getRequest().getRequestId().equals(batchRequest.getReference())) {
                final BatchRequestNode dependingRequest = new BatchRequestNode();
                dependingRequest.setRequest(batchRequest);
                batchRequestNode.addChildRequest(dependingRequest);
            } else {
                addDependingRequest(batchRequest, batchRequestNode.getChildRequests());
            }
        }
    }

    /**
     * Returns a BatchRequest after dependency resolution. It takes a request
     * and the response of the request it is dependent upon as its arguments and
     * change the body or relativeUrl of the request according to parent
     * Request.
     * 
     * @param request
     * @param lastResponse
     * @return BatchRequest
     */
    public BatchRequest resoluteRequest(final BatchRequest request, final BatchResponse parentResponse) {

        // Create a duplicate request
        final BatchRequest br = request;

        final JsonModel responseJsonModel = JsonModel.model(parentResponse.getBody());

        // Gets the body from current Request as a JsonObject
        final JsonObject jsonRequestBody = this.fromJsonHelper.parse(request.getBody()).getAsJsonObject();

        JsonObject jsonResultBody = new JsonObject();

        // Iterate through each element in the requestBody to find dependent
        // parameter
        for (Entry<String, JsonElement> element : jsonRequestBody.entrySet()) {
            final String key = element.getKey();
            final JsonElement value = resolveDependentVariables(element, responseJsonModel);
            jsonResultBody.add(key, value);
        }

        // Set the body after dependency resolution
        br.setBody(jsonResultBody.toString());

        // Also check the relativeUrl for any dependency resolution
        String relativeUrl = request.getRelativeUrl();

        if (relativeUrl.contains("$.")) {

            String queryParams = "";
            if(relativeUrl.contains("?")) {
                queryParams = relativeUrl.substring(relativeUrl.indexOf("?"));
                relativeUrl = relativeUrl.substring(0, relativeUrl.indexOf("?"));
            }
            
            final String[] parameters = relativeUrl.split("/");
            
            for (String parameter : parameters) {
                if (parameter.contains("$.")) {
                    final String resParamValue = responseJsonModel.get(parameter).toString();
                    relativeUrl = relativeUrl.replace(parameter, resParamValue);
                    br.setRelativeUrl(relativeUrl+queryParams);
                }
            }
        }

        return br;
    }

    private JsonElement resolveDependentVariables(final Entry<String, JsonElement> entryElement, final JsonModel responseJsonModel) {
        JsonElement value = null;

        final JsonElement element = entryElement.getValue();

        if (element.isJsonObject()) {
            final JsonObject jsObject = element.getAsJsonObject();
            value = processJsonObject(jsObject, responseJsonModel);
        } else if (element.isJsonArray()) {
            final JsonArray jsElementArray = element.getAsJsonArray();
            value = processJsonArray(jsElementArray, responseJsonModel);
        } else {
            value = resolveDependentVariable(element, responseJsonModel);
        }
        return value;
    }

    private JsonElement processJsonObject(final JsonObject jsObject, final JsonModel responseJsonModel) {
        JsonObject valueObj = new JsonObject();
        for (Entry<String, JsonElement> element : jsObject.entrySet()) {
            final String key = element.getKey();
            final JsonElement value = resolveDependentVariable(element.getValue(), responseJsonModel);
            valueObj.add(key, value);
        }
        return valueObj;
    }

    private JsonArray processJsonArray(final JsonArray elementArray, final JsonModel responseJsonModel) {

        JsonArray valueArr = new JsonArray();

        for (JsonElement element : elementArray) {
            if (element.isJsonObject()) {
                final JsonObject jsObject = element.getAsJsonObject();
                valueArr.add(processJsonObject(jsObject, responseJsonModel));
            }
        }

        return valueArr;
    }

    private JsonElement resolveDependentVariable(final JsonElement element, final JsonModel responseJsonModel) {
        JsonElement value = element;
        String paramVal = element.getAsString();
        if (paramVal.contains("$.")) {
            // Get the value of the parameter from parent response
            final String resParamValue = responseJsonModel.get(paramVal).toString();
            value = this.fromJsonHelper.parse(resParamValue);
        }
        return value;
    }

}
