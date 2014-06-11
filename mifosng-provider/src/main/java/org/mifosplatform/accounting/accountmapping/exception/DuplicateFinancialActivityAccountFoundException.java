/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.exception;

import org.mifosplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

/**
 * A {@link RuntimeException} thrown when product to GL account mapping are not
 * found.
 */
public class DuplicateFinancialActivityAccountFoundException extends AbstractPlatformDomainRuleException {

    public DuplicateFinancialActivityAccountFoundException(final Integer financialActivityType) {
        super("error.msg.officeToAccountMapping.exists.for.office", "Mapping for activity already exists " + financialActivityType,
                financialActivityType);
    }

}