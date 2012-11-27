package org.mifosplatform.accounting.api.infrastructure;

import java.util.Collection;
import java.util.Set;

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.accounting.api.data.GLClosureData;

public interface AccountingApiJsonSerializerService {

    String serializeEntityIdentifier(final EntityIdentifier identifier);

    String serializeGLAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, GLAccountData accountData);

    String serializeGLAccountDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GLAccountData> accountDatas);

    String serializeGLClosureDataToJson(boolean prettyPrint, Set<String> responseParameters, GLClosureData closureData);

    String serializeGLClosureDataToJson(boolean prettyPrint, Set<String> responseParameters, Collection<GLClosureData> closureDatas);

}