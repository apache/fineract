package org.mifosplatform.accounting.service;

import java.util.Map;

public interface ProductToGLAccountMappingReadPlatformService {

    Map<String, Object> fetchAccountMappingDetailsForLoanProduct(final Long loanProductId, final Integer accountingType);
}