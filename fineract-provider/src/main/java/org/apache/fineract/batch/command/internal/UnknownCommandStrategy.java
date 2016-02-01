/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.command.internal;

import javax.ws.rs.core.UriInfo;

import org.mifosplatform.batch.command.CommandStrategy;
import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;
import org.springframework.stereotype.Component;

/**
 * Provides a default CommandStrategy by implementing
 * {@link org.mifosplatform.batch.command.CommandStrategy} in case there is no
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
