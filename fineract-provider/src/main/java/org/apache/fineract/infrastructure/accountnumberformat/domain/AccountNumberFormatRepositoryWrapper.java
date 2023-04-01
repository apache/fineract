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
package org.apache.fineract.infrastructure.accountnumberformat.domain;

import lombok.AllArgsConstructor;
import org.apache.fineract.infrastructure.accountnumberformat.exception.AccountNumberFormatNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class AccountNumberFormatRepositoryWrapper {

    private final AccountNumberFormatRepository repository;

    public AccountNumberFormat findOneWithNotFoundDetection(final Long id) {
        return this.repository.findById(id).orElseThrow(() -> new AccountNumberFormatNotFoundException(id));
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
