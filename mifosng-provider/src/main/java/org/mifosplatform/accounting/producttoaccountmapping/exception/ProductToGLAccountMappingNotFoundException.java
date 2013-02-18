/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.producttoaccountmapping.exception;

import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class ProductToGLAccountMappingNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ProductToGLAccountMappingNotFoundException(final PortfolioProductType type, final Long productId, final String accountType) {
        super("error.msg.productTyAccountMapping.not.found", "Mapping for product of type" + type.toString() + " with Id " + productId
                + " does not exist for an account of type " + accountType, type.toString(), productId, accountType);
    }
}