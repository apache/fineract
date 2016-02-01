/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.productmix.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when no product mixes found with the
 * productId.
 */
public class ProductMixNotFoundException extends AbstractPlatformResourceNotFoundException {

    public ProductMixNotFoundException(final Long productId) {
        super("error.msg.no.product.mixes.exists", "No product mixes are defined with the productId `" + productId + "`.", productId);
    }

}
