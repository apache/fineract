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
package org.apache.fineract.portfolio.workingcapitalloan.transaction.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnumConverter;
import org.apache.fineract.portfolio.workingcapitalloan.charge.domain.WorkingCapitalLoanCharge;

@Getter
@Entity
@Table(name = "m_wc_loan_transaction_relation")
public class WorkingCapitalLoanTransactionRelation extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "from_loan_transaction_id", nullable = false)
    private WorkingCapitalLoanTransaction fromTransaction;

    @Setter
    @ManyToOne
    @JoinColumn(name = "to_loan_transaction_id")
    private WorkingCapitalLoanTransaction toTransaction;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_loan_charge_id")
    private WorkingCapitalLoanCharge toCharge;

    @Column(name = "relation_type_enum", nullable = false)
    @Convert(converter = LoanTransactionRelationTypeEnumConverter.class)
    private LoanTransactionRelationTypeEnum relationType;

    protected WorkingCapitalLoanTransactionRelation() {}

    public WorkingCapitalLoanTransactionRelation(@NotNull WorkingCapitalLoanTransaction fromTransaction,
            WorkingCapitalLoanTransaction toTransaction, LoanTransactionRelationTypeEnum relationType) {
        this.fromTransaction = fromTransaction;
        this.toTransaction = toTransaction;
        this.relationType = relationType;
    }

    public static WorkingCapitalLoanTransactionRelation linkToCharge(@NotNull WorkingCapitalLoanTransaction fromTransaction,
            @NotNull WorkingCapitalLoanCharge charge, LoanTransactionRelationTypeEnum relationType) {
        final WorkingCapitalLoanTransactionRelation relation = new WorkingCapitalLoanTransactionRelation(fromTransaction, null,
                relationType);
        relation.toCharge = charge;
        return relation;
    }

}
