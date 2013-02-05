package org.mifosplatform.accounting.api.infrastructure.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.accounting.api.data.GLClosureData;
import org.mifosplatform.accounting.api.data.GLJournalEntryData;
import org.mifosplatform.accounting.api.data.JournalEntryIdentifier;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.mifosplatform.portfolio.savingsaccount.PortfolioApiJsonSerializerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * Implementation of {@link PortfolioApiJsonSerializerService} that uses
 * google-gson to serialize Java object representation into JSON.
 */
@Service
public class GoogleGsonAccountingApiJsonSerializerService implements AccountingApiJsonSerializerService {

    private static final Set<String> DATA_PARAMETERS = new HashSet<String>(Arrays.asList("entityId"));

    private static final Set<String> GL_ACCOUNT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "parentId", "glCode",
            "disabled", "manualEntriesAllowed", "classification", "headerAccount", "description"));

    private static final Set<String> GL_ACCOUNT_CLOSURE_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "officeId", "officeName",
            "closingDate", "deleted", "createdDate", "lastUpdatedDate", "createdByUserId", "createdByUsername", "lastUpdatedByUserId",
            "lastUpdatedByUsername", "comments", "allowedOffices"));

    private static final Set<String> GL_JOURNAL_ENTRY_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "officeId", "officeName",
            "glAccountName", "glAccountId", "glAccountCode", "glAccountClassification", "entryDate", "entryType", "amount",
            "transactionId", "portfolioGenerated", "entityType", "entityId", "createdByUserId", "createdDate", "createdByUserName",
            "comments", "reversed"));

    private final GoogleGsonSerializerHelper helper;

    @Autowired
    public GoogleGsonAccountingApiJsonSerializerService(final GoogleGsonSerializerHelper helper) {
        this.helper = helper;
    }

    @Override
    public String serializeEntityIdentifier(final CommandProcessingResult identifier) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(DATA_PARAMETERS, false,
                DATA_PARAMETERS);
        return helper.serializedJsonFrom(gsonDeserializer, identifier);
    }

    @Override
    public String serializeJournalEntryIdentifier(final JournalEntryIdentifier identifier) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(DATA_PARAMETERS, false,
                DATA_PARAMETERS);
        return helper.serializedJsonFrom(gsonDeserializer, identifier);
    }

    @Override
    public String serializeGLAccountDataToJson(final boolean prettyPrint, final Set<String> responseParameters,
            final GLAccountData accountData) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GL_ACCOUNT_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, accountData);
    }

    @Override
    public String serializeGLAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GLAccountData> accountDatas) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GL_ACCOUNT_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, accountDatas.toArray(new GLAccountData[accountDatas.size()]));
    }

    @Override
    public String serializeGLClosureDataToJson(boolean prettyPrint, Set<String> responseParameters, GLClosureData closureData) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                GL_ACCOUNT_CLOSURE_DATA_PARAMETERS, prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, closureData);
    }

    @Override
    public String serializeGLClosureDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GLClosureData> closureDatas) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(
                GL_ACCOUNT_CLOSURE_DATA_PARAMETERS, prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, closureDatas.toArray(new GLClosureData[closureDatas.size()]));
    }

    @Override
    public String serializeGLJournalEntryDataToJson(boolean prettyPrint, Set<String> responseParameters, GLJournalEntryData journalEntryData) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GL_JOURNAL_ENTRY_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, journalEntryData);
    }

    @Override
    public String serializeGLJournalEntryDataToJson(boolean prettyPrint, Set<String> responseParameters,
            Collection<GLJournalEntryData> journalEntryDatas) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GL_JOURNAL_ENTRY_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, journalEntryDatas.toArray(new GLJournalEntryData[journalEntryDatas.size()]));
    }

}