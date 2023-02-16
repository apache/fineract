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
package org.apache.fineract.portfolio.client.domain;

import java.math.BigDecimal;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;

@Entity
@Table(name = "m_client_transaction_limit")
public class ClientTransactionLimit extends AbstractPersistableCustom {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", insertable = false, updatable = false)
    private Client client;

    @Column(name = "client_id")
    private Long clientId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_level_id", insertable = false, updatable = false)
    private CodeValue clientLevel;

    @Column(name = "client_level_id")
    private Long clientLevelId;

    @Column(name = "daily_withdraw_limit")
    private BigDecimal dailyWithdrawLimit;

    @Column(name = "single_withdraw_limit")
    private BigDecimal singleWithdrawLimit;

    public ClientTransactionLimit() {}

    public ClientTransactionLimit(Client client, Long clientLevelId, BigDecimal dailyWithdrawLimit, BigDecimal singleWithdrawLimit) {
        this.setClient(client);
        this.clientLevelId = clientLevelId;
        this.dailyWithdrawLimit = dailyWithdrawLimit;
        this.singleWithdrawLimit = singleWithdrawLimit;
    }

    public void update(final JsonCommand command) {
        if (command.isChangeInLongParameterNamed(ClientApiConstants.clientLevelIdParamName, getClientLevelId())) {
            final Long newValue = command.longValueOfParameterNamed(ClientApiConstants.clientLevelIdParamName);
            this.clientLevelId = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(ClientApiConstants.dailyWithdrawLimit, getDailyWithdrawLimit())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(ClientApiConstants.dailyWithdrawLimit);
            this.dailyWithdrawLimit = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(ClientApiConstants.singleWithdrawLimit, getDailyWithdrawLimit())) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(ClientApiConstants.singleWithdrawLimit);
            this.singleWithdrawLimit = newValue;
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.setClientId(client == null ? null : client.getId());
        this.client = client;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getClientLevelId() {
        return clientLevelId;
    }

    public BigDecimal getDailyWithdrawLimit() {
        return this.dailyWithdrawLimit == null ? BigDecimal.ZERO : this.dailyWithdrawLimit;
    }

    public BigDecimal getSingleWithdrawLimit() {
        return this.singleWithdrawLimit == null ? BigDecimal.ZERO : this.singleWithdrawLimit;
    }
}
