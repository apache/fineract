/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.commands.data;

/**
 * Immutable data object for application user data.
 */
public class ProcessingResultLookup {

    private final Long id;
    private final String processingResult;

    public ProcessingResultLookup(final Long id, final String processingResult) {
        this.id = id;
        this.processingResult = processingResult;
    }

    public Long getId() {
        return this.id;
    }

    public String getProcessingResult() {
        return this.processingResult;
    }

}