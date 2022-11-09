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
package org.apache.fineract.portfolio.loanaccount.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@Entity
@Table(name = "m_loan_transaction_relation")
public class LoanTransactionRelation extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "from_loan_transaction_id", nullable = false)
    private LoanTransaction fromTransaction;

    @ManyToOne
    @JoinColumn(name = "to_loan_transaction_id")
    private LoanTransaction toTransaction;

    @ManyToOne
    @JoinColumn(name = "to_loan_charge_id")
    private LoanCharge toCharge;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "relation_type_enum", nullable = false)
    private LoanTransactionRelationTypeEnum relationType;

    @Version
    private Long version;

    protected LoanTransactionRelation() {}

    protected LoanTransactionRelation(@NotNull LoanTransaction fromTransaction, LoanTransaction toTransaction, LoanCharge toCharge,
            LoanTransactionRelationTypeEnum relationType) {
        this.fromTransaction = fromTransaction;
        this.toTransaction = toTransaction;
        this.toCharge = toCharge;
        this.relationType = relationType;
    }

    public static LoanTransactionRelation linkToTransaction(@NotNull LoanTransaction fromTransaction,
            @NotNull LoanTransaction toTransaction, LoanTransactionRelationTypeEnum relation) {
        return new LoanTransactionRelation(fromTransaction, toTransaction, null, relation);
    }

    public static LoanTransactionRelation linkToCharge(@NotNull LoanTransaction fromTransaction, @NotNull LoanCharge loanCharge,
            LoanTransactionRelationTypeEnum relation) {
        return new LoanTransactionRelation(fromTransaction, null, loanCharge, relation);
    }
}
