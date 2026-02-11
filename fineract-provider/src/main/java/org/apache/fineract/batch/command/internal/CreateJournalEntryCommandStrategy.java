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

import static org.apache.fineract.batch.command.CommandStrategyUtils.relativeUrlWithoutVersion;

import com.google.common.base.Splitter;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.journalentry.api.JournalEntriesApiResource;
import org.apache.fineract.batch.command.CommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Implements {@link org.apache.fineract.batch.command.CommandStrategy} to handle creation of journal entries. It
 * passes the contents of the body from the BatchRequest to
 * {@link org.apache.fineract.accounting.journalentry.api.JournalEntriesApiResource} and gets back the response. This
 * class will also catch any errors raised by {@link org.apache.fineract.accounting.journalentry.api.JournalEntriesApiResource}
 * and map those errors to appropriate status codes in BatchResponse.
 *
 * @see org.apache.fineract.batch.command.CommandStrategy
 * @see org.apache.fineract.batch.domain.BatchRequest
 * @see org.apache.fineract.batch.domain.BatchResponse
 */
@Component
@RequiredArgsConstructor
public class CreateJournalEntryCommandStrategy implements CommandStrategy {

    private final JournalEntriesApiResource journalEntriesApiResource;

    @Override
    public BatchResponse execute(final BatchRequest request, @SuppressWarnings("unused") UriInfo uriInfo) {

        final BatchResponse response = new BatchResponse();
        final String responseBody;

        response.setRequestId(request.getRequestId());
        response.setHeaders(request.getHeaders());

        // Extract command parameter if present
        String commandParam = null;
        final List<String> pathParameters = Splitter.on('/').splitToList(relativeUrlWithoutVersion(request));
        
        // Check if there's a query parameter with command
        if (pathParameters.size() > 1) {
            final String lastPart = pathParameters.get(pathParameters.size() - 1);
            final Pattern commandPattern = Pattern.compile("\\?command=([\\w\\-]+)");
            final Matcher commandMatcher = commandPattern.matcher(lastPart);
            
            if (commandMatcher.find()) {
                commandParam = commandMatcher.group(1);
            }
        }

        // Calls 'createGLJournalEntry' function from 'JournalEntriesApiResource' to create a new journal entry
        responseBody = journalEntriesApiResource.createGLJournalEntry(request.getBody(), commandParam);

        response.setStatusCode(HttpStatus.SC_OK);
        // Sets the body of the response after the successful creation of the journal entry
        response.setBody(responseBody);

        return response;
    }
}
