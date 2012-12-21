package org.mifosplatform.audit.api;

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