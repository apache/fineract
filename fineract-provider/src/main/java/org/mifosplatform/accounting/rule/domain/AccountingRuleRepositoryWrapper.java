/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.rule.domain;

import org.mifosplatform.accounting.rule.exception.AccountingRuleNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link AccountingRuleRepository} .
 * </p>
 */
@Service
public class AccountingRuleRepositoryWrapper {

    private final AccountingRuleRepository repository;

    @Autowired
    public AccountingRuleRepositoryWrapper(final AccountingRuleRepository repository) {
        this.repository = repository;
    }

    public AccountingRule findOneWithNotFoundDetection(final Long id) {
        final AccountingRule accountingRule = this.repository.findOne(id);
        if (accountingRule == null) { throw new AccountingRuleNotFoundException(id); }
        return accountingRule;
    }

}