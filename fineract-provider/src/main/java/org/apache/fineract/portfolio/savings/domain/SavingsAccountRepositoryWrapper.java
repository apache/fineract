/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import org.mifosplatform.portfolio.savings.DepositAccountType;
import org.mifosplatform.portfolio.savings.exception.SavingsAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link SavingsAccountRepository} that is responsible for checking
 * if {@link SavingsAccount} is returned when using <code>findOne</code>
 * repository method and throwing an appropriate not found exception.
 * </p>
 * 
 * <p>
 * This is to avoid need for checking and throwing in multiple areas of code
 * base where {@link SavingsAccountRepository} is required.
 * </p>
 */
@Service
public class SavingsAccountRepositoryWrapper {

    private final SavingsAccountRepository repository;

    @Autowired
    public SavingsAccountRepositoryWrapper(final SavingsAccountRepository repository) {
        this.repository = repository;
    }

    public SavingsAccount findOneWithNotFoundDetection(final Long savingsId) {
        final SavingsAccount account = this.repository.findOne(savingsId);
        if (account == null) { throw new SavingsAccountNotFoundException(savingsId); }
        return account;
    }

    public SavingsAccount findOneWithNotFoundDetection(final Long savingsId, final DepositAccountType depositAccountType) {
        final SavingsAccount account = this.repository.findByIdAndDepositAccountType(savingsId, depositAccountType.getValue());
        if (account == null) { throw new SavingsAccountNotFoundException(savingsId); }
        return account;
    }

    public void save(final SavingsAccount account) {
        this.repository.save(account);
    }

    public void delete(final SavingsAccount account) {
        this.repository.delete(account);
    }

    public void saveAndFlush(final SavingsAccount account) {
        this.repository.saveAndFlush(account);
    }
}