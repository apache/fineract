/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.service;

import java.util.Map;

public interface ProductToGLAccountMappingReadPlatformService {

    Map<String, Object> fetchAccountMappingDetailsForLoanProduct(final Long loanProductId, final Integer accountingType);
}