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
package org.apache.fineract.accounting.financialactivityaccount.domain;

import java.util.List;
import org.apache.fineract.accounting.financialactivityaccount.exception.FinancialActivityAccountNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * Wrapper for {@link FinancialActivityAccountRepository} that adds NULL checking and Error handling capabilities
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
        return this.repository.findById(id).orElseThrow(() -> new FinancialActivityAccountNotFoundException(id));
    }

    public FinancialActivityAccount findByFinancialActivityTypeWithNotFoundDetection(final int financialActivityType) {
        FinancialActivityAccount financialActivityAccount = this.repository.findByFinancialActivityType(financialActivityType);
        if (financialActivityAccount == null) {
            throw new FinancialActivityAccountNotFoundException(financialActivityType);
        }
        return financialActivityAccount;
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
