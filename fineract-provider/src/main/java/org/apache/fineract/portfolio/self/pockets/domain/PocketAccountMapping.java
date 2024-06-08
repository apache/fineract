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

package org.apache.fineract.portfolio.self.pockets.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@SuppressWarnings("serial")
@Entity
@Table(name = "m_pocket_accounts_mapping")
public class PocketAccountMapping extends AbstractPersistableCustom<Long> {

    @Column(name = "pocket_id", length = 20, nullable = false)
    private Long pocketId;

    @Column(name = "account_id", length = 20, nullable = false)
    private Long accountId;

    @Column(name = "account_type", nullable = false)
    private Integer accountType;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    protected PocketAccountMapping() {}

    private PocketAccountMapping(final Long pocketId, final Long accountId, final Integer accountType, final String accountNumber) {
        this.pocketId = pocketId;
        this.accountId = accountId;
        this.accountType = accountType;
        this.accountNumber = accountNumber;
    }

    public static PocketAccountMapping instance(final Long pocketId, final Long accountId, final Integer accountType,
            final String accountNumber) {
        return new PocketAccountMapping(pocketId, accountId, accountType, accountNumber);

    }

    public Long getPocketId() {
        return pocketId;
    }

    public void setPocketId(Long pocketId) {
        this.pocketId = pocketId;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public void setAccountType(Integer accountType) {
        this.accountType = accountType;
    }

}
