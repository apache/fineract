/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.accountmapping.domain;

import java.util.List;

import org.mifosplatform.accounting.accountmapping.exception.FinancialActivityAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link FinancialActivityAccountRepository} that adds NULL
 * checking and Error handling capabilities
 * </p>
 */
@Service
public class FinancialActivityAccountRepositoryWrapper {

    private final FinancialActivityAccountRepository repository;

    @Autowired
    public FinancialActivityAccountRepositoryWrapper(final FinancialActivityAccountRepository repository) {
        this.repository = repository;
    }

    public FinancialActivityAccount findOneWithNotFoundDetection(final Long id) {
        final FinancialActivityAccount financialActivityAccount = this.repository.findOne(id);
        if (financialActivityAccount == null) { throw new FinancialActivityAccountNotFoundException(id); }
        return financialActivityAccount;
    }

    public FinancialActivityAccount findByFinancialActivityType(final int financialActivityType) {
        return this.repository.findByFinancialActivityType(financialActivityType);
    }

    public List<FinancialActivityAccount> findAll() {
        return this.repository.findAll();
    }

    public void save(final FinancialActivityAccount entity) {
        this.repository.save(entity);
    }

    public void saveAndFlush(final FinancialActivityAccount entity) {
        this.repository.saveAndFlush(entity);
    }

    public void delete(final FinancialActivityAccount entity) {
        this.repository.delete(entity);
    }
}