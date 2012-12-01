package org.mifosplatform.accounting.api.infrastructure.impl;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.accounting.api.data.GLClosureData;
import org.mifosplatform.accounting.api.infrastructure.AccountingApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.api.GoogleGsonSerializerHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.EntityIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;

/**
 * Implementation of {@link PortfolioApiJsonSerializerService} that uses
 * google-gson to serialize Java object representation into JSON.
 */
@Service
public class GoogleGsonAccountingApiJsonSerializerService implements AccountingApiJsonSerializerService {

    private static final Set<String> GL_ACCOUNT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "name", "parentId", "glCode",
            "disabled", "manualEntriesAllowed", "classification", "headerAccount", "description"));

    private static final Set<String> GL_ACCOUNT_CLOSURE_PARAMETERS = new HashSet<String>(Arrays.asList("id", "officeId", "officeName",
            "closingDate", "deleted", "createdDate", "lastUpdatedDate", "creatingByUserId", "lastUpdatedByUserId", "comments"));

    private final GoogleGsonSerializerHelper helper;

    @Autowired
    public GoogleGsonAccountingApiJsonSerializerService(final GoogleGsonSerializerHelper helper) {
        this.helper = helper;
    }

    @Override
    public String serializeEntityIdentifier(final EntityIdentifier identifier) {
        final Set<String> DATA_PARAMETERS = new HashSet<String>(Arrays.asList("entityId"));
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
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GL_ACCOUNT_CLOSURE_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, closureData);
    }

    @Override
    public String serializeGLClosureDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GLClosureData> closureDatas) {
        final Gson gsonDeserializer = helper.createGsonBuilderWithParameterExclusionSerializationStrategy(GL_ACCOUNT_DATA_PARAMETERS,
                prettyPrint, responseParameters);
        return helper.serializedJsonFrom(gsonDeserializer, closureDatas.toArray(new GLAccountData[closureDatas.size()]));
    }
}