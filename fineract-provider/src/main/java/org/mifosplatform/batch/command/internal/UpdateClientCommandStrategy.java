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
import org.mifosplatform.batch.exception.ErrorHandler;
import org.mifosplatform.batch.exception.ErrorInfo;
import org.mifosplatform.portfolio.client.api.ClientsApiResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Implements {@link org.mifosplatform.batch.command.CommandStrategy} and
 * updates the information of an existing client. It passes the contents of the
 * body from the BatchRequest to
 * {@link org.mifosplatform.portfolio.client.api.ClientsApiResource} and gets
 * back the response. This class will also catch any errors raised by
 * {@link org.mifosplatform.portfolio.client.api.ClientsApiResource} and map
 * those errors to appropriate status codes in BatchResponse.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.command.CommandStrategy
 * @see org.mifosplatform.batch.domain.BatchRequest
 * @see org.mifosplatform.batch.domain.BatchResponse
 */
@Component
public class UpdateClientCommandStrategy implements CommandStrategy {

    private final ClientsApiResource clientsApiResource;

    @Autowired
    public UpdateClientCommandStrategy(final ClientsApiResource clientsApiResource) {
        this.clientsApiResource = clientsApiResource;
    }

    @Override
    public BatchResponse execute(BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        // Get the clientID
        final String relativeUrl = request.getRelativeUrl();
        final Long clientId = Long.parseLong(relativeUrl.substring(relativeUrl.indexOf('/') + 1));

        // Try-catch blocks to map exceptions to appropriate status codes
        try {

            // Calls 'update' function from 'ClientsApiResource' to update a
            // client
            responseBody = clientsApiResource.update(clientId, request.getBody());

            response.setStatusCode(200);
            // Sets the body of the response after the successful update of
            // client information
            response.setBody(responseBody);

        } catch (RuntimeException e) {

            // Gets an object of type ErrorInfo, containing information about
            // raised exception
            ErrorInfo ex = ErrorHandler.handler(e);

            response.setStatusCode(ex.getStatusCode());
            response.setBody(ex.getMessage());
        }

        return response;
    }

}
