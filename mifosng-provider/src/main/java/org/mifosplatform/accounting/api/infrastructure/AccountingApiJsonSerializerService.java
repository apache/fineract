package org.mifosplatform.accounting.api.infrastructure;

import java.util.Collection;
import java.util.Set;

import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.accounting.api.data.GLClosureData;
import org.mifosplatform.accounting.api.data.GLJournalEntryData;
import org.mifosplatform.accounting.api.data.JournalEntryIdentifier;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;


public interface AccountingApiJsonSerializerService {

    String serializeEntityIdentifier(final CommandProcessingResult identifier);

    String serializeJournalEntryIdentifier(final JournalEntryIdentifier identifier);

    String serializeGLAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, GLAccountData accountData);

    String serializeGLAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GLAccountData> accountDatas);

    String serializeGLClosureDataToJson(boolean prettyPrint, Set<String> responseParameters, GLClosureData closureData);

    String serializeGLClosureDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GLClosureData> closureDatas);

    String serializeGLJournalEntryDataToJson(boolean prettyPrint, Set<String> responseParameters, GLJournalEntryData journalEntryData);

    String serializeGLJournalEntryDataToJson(boolean prettyPrint, Set<String> responseParameters,
            Collection<GLJournalEntryData> journalEntryDatas);

}