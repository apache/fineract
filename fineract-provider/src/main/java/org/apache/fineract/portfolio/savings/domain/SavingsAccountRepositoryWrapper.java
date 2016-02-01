/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.domain;

import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.exception.SavingsAccountNotFoundException;
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