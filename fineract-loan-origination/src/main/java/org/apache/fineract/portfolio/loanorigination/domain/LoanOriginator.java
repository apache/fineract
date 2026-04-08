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
package org.apache.fineract.portfolio.loanorigination.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.domain.ExternalId;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "m_loan_originator")
public class LoanOriginator extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "external_id", nullable = false, length = 100, unique = true)
    private ExternalId externalId;

    @Column(name = "name", length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LoanOriginatorStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "originator_type_cv_id")
    private CodeValue originatorType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_type_cv_id")
    private CodeValue channelType;

    public static LoanOriginator create(ExternalId externalId, String name, LoanOriginatorStatus status, CodeValue originatorType,
            CodeValue channelType) {
        LoanOriginator originator = new LoanOriginator();
        originator.setExternalId(externalId);
        originator.setName(name);
        originator.setStatus(status);
        originator.setOriginatorType(originatorType);
        originator.setChannelType(channelType);
        return originator;
    }

    public void update(String name, LoanOriginatorStatus status, CodeValue originatorType, CodeValue channelType) {
        this.name = name;
        this.status = status;
        this.originatorType = originatorType;
        this.channelType = channelType;
    }
}
