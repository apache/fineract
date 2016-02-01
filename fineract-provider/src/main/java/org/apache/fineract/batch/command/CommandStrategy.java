/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.batch.command;

import javax.ws.rs.core.UriInfo;

import org.mifosplatform.batch.domain.BatchRequest;
import org.mifosplatform.batch.domain.BatchResponse;

/**
 * An interface for various Command Strategies. It contains a single function
 * which returns appropriate response from a particular command strategy.
 * 
 * @author Rishabh Shukla
 * 
 * @see org.mifosplatform.batch.command.internal.UnknownCommandStrategy
 */
public interface CommandStrategy {

    /**
     * Returns an object of type
     * {@link org.mifosplatform.batch.domain.BatchResponse}. This takes
     * {@link org.mifosplatform.batch.domain.BatchRequest} as it's single
     * argument and provides appropriate response.
     * 
     * @param batchRequest
     * @param uriInfo
     * @return BatchResponse
     */
    public BatchResponse execute(BatchRequest batchRequest, UriInfo uriInfo);
}
