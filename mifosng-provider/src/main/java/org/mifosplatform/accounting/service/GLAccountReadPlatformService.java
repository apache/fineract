package org.mifosplatform.accounting.service;

import java.util.List;

import org.mifosplatform.accounting.api.data.GLAccountData;

public interface GLAccountReadPlatformService {

    List<GLAccountData> retrieveAllGLAccounts(String accountClassification, String searchParam, String usage,
            Boolean manualTransactionsAllowed, Boolean disabled);

    GLAccountData retrieveGLAccountById(long glAccountId);

}
