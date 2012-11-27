package org.mifosplatform.accounting.api.commands;

import java.util.Set;

import org.joda.time.LocalDate;

/**
 * Immutable command for adding an accounting closure
 */
public class GLClosureCommand {

    private final Long id;
    private final Long officeId;
    private final LocalDate closingDate;
    private final String comments;

    private final Set<String> parametersPassedInRequest;

    public GLClosureCommand(final Set<String> modifiedParameters, final Long id, final Long officeId, final LocalDate closingDate,
            final String comments) {
        this.id = id;
        this.officeId = officeId;
        this.closingDate = closingDate;
        this.comments = comments;
        this.parametersPassedInRequest = modifiedParameters;
    }

    public boolean isCommentsChanged() {
        return this.parametersPassedInRequest.contains("comments");
    }

    public Long getId() {
        return this.id;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public LocalDate getClosingDate() {
        return this.closingDate;
    }

    public String getComments() {
        return this.comments;
    }

}