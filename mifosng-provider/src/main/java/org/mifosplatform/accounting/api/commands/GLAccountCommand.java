package org.mifosplatform.accounting.api.commands;

import java.util.Set;

/**
 * Immutable command for adding a general Ledger Account
 */
public class GLAccountCommand {

    private final Long id;
    private final String name;
    private final Long parentId;
    private final String glCode;
    private final Boolean disabled;
    private final Boolean manualEntriesAllowed;
    private final Boolean headerAccount;
    private final String classification;
    private final String description;

    private final Set<String> parametersPassedInRequest;

    public GLAccountCommand(final Set<String> modifiedParameters, final Long id, final String name, final Long parentId,
            final String glCode, final Boolean disabled, final Boolean manualEntriesAllowed, final String classification,
            final Boolean headerAccount, final String description) {
        this.parametersPassedInRequest = modifiedParameters;
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.glCode = glCode;
        this.disabled = disabled;
        this.manualEntriesAllowed = manualEntriesAllowed;
        this.classification = classification;
        this.headerAccount = headerAccount;
        this.description = description;
    }

    public boolean isNameChanged() {
        return this.parametersPassedInRequest.contains("name");
    }

    public boolean isParentIdChanged() {
        return this.parametersPassedInRequest.contains("parentId");
    }

    public boolean isGLCodeChanged() {
        return this.parametersPassedInRequest.contains("glCode");
    }

    public boolean isDisabledFlagChanged() {
        return this.parametersPassedInRequest.contains("disabled");
    }

    public boolean isManualEntriesAllowedFlagChanged() {
        return this.parametersPassedInRequest.contains("manualEntriesAllowed");
    }

    public boolean isClassificationChanged() {
        return this.parametersPassedInRequest.contains("classification");
    }

    public boolean isHeaderAccountFlagChanged() {
        return this.parametersPassedInRequest.contains("headerAccount");
    }

    public boolean isDescriptionChanged() {
        return this.parametersPassedInRequest.contains("description");
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getParentId() {
        return parentId;
    }

    public String getGlCode() {
        return glCode;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public Boolean getManualEntriesAllowed() {
        return manualEntriesAllowed;
    }

    public Boolean getHeaderAccount() {
        return headerAccount;
    }

    public String getDescription() {
        return description;
    }

    public String getClassification() {
        return classification;
    }
}