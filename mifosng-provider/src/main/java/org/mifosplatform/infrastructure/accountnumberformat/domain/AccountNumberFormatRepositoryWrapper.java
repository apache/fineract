/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.domain;

import org.mifosplatform.infrastructure.accountnumberformat.exception.AccountNumberFormatNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AccountNumberFormatRepositoryWrapper {

    private final AccountNumberFormatRepository repository;

    @Autowired
    public AccountNumberFormatRepositoryWrapper(final AccountNumberFormatRepository repository) {
        this.repository = repository;
    }

    public AccountNumberFormat findOneWithNotFoundDetection(final Long id) {
        final AccountNumberFormat accountNumberFormat = this.repository.findOne(id);
        if (accountNumberFormat == null) { throw new AccountNumberFormatNotFoundException(id); }
        return accountNumberFormat;
    }

    public void save(final AccountNumberFormat accountNumberFormat) {
        this.repository.save(accountNumberFormat);
    }

    public void saveAndFlush(final AccountNumberFormat accountNumberFormat) {
        this.repository.saveAndFlush(accountNumberFormat);
    }

    public void delete(final AccountNumberFormat accountNumberFormat) {
        this.repository.delete(accountNumberFormat);
    }

    public AccountNumberFormat findByAccountType(final EntityAccountType entityAccountType) {
        return this.repository.findOneByAccountTypeEnum(entityAccountType.getValue());
    }
}