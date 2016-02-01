/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.batch.command.internal;

import javax.ws.rs.core.UriInfo;

import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.springframework.stereotype.Component;

/**
 * Provides a default CommandStrategy by implementing
 * {@link org.apache.fineract.batch.command.CommandStrategy} in case there is no
 * appropriate command strategy with requested 'method' and 'resoureUrl'.
 * 
 * @author Rishabh Shukla
 */
@Component
public class UnknownCommandStrategy implements CommandStrategy {

    @Override
    public BatchResponse execute(BatchRequest batchRequest, @SuppressWarnings("unused") UriInfo uriInfo) {

        final BatchResponse batchResponse = new BatchResponse();

        batchResponse.setRequestId(batchRequest.getRequestId());
        batchResponse.setStatusCode(501);
        batchResponse.setBody("Resource with method " + batchRequest.getMethod() + " and relativeUrl " + batchRequest.getRelativeUrl()
                + " doesn't exist");

        return batchResponse;
    }

}
