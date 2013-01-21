package org.mifosplatform.accounting.service;

import java.util.List;

import org.mifosplatform.accounting.api.data.GLAccountData;
import org.mifosplatform.accounting.domain.GLAccountType;

public interface GLAccountReadPlatformService {

    List<GLAccountData> retrieveAllGLAccounts(Integer accountClassification, String searchParam, Integer usage,
            Boolean manualTransactionsAllowed, Boolean disabled);

    GLAccountData retrieveGLAccountById(long glAccountId);

    List<GLAccountData> retrieveAllEnabledDetailGLAccounts(GLAccountType accountType);

    GLAccountData retrieveNewGLAccountDetails();

}
