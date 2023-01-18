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

import org.apache.fineract.portfolio.accountdetails.data.RevokedInterestTransactionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface VaultTribeCustomSavingsAccountTransactionRepository
        extends JpaRepository<RevokedInterestTransactionData, Long>, JpaSpecificationExecutor<RevokedInterestTransactionData> {

    @Query(value = "SELECT tx.id  AS id, tx.payment_detail_id AS paymentDetailId, tx.savings_account_id AS savingsAccountId, dp.actual_transaction_type As actualTransactionType, tx.is_reversed  AS isReversed, tx.transaction_type_enum  AS transactionType FROM m_payment_detail dp INNER JOIN m_savings_account_transaction tx ON dp.id = tx.payment_detail_id WHERE dp.parent_savings_account_transaction_id = ?1 AND dp.parent_transaction_payment_details_id = ?2 AND tx.transaction_type_enum = 2", nativeQuery = true)
    RevokedInterestTransactionData findRevokedInterestTransaction(Long transactionId, Long paymentDetailsId);

    @Query(value = "SELECT tx.id   AS id, tx.payment_detail_id  AS paymentDetailId,  tx.savings_account_id   AS savingsAccountId, dp.actual_transaction_type As actualTransactionType, tx.is_reversed  AS isReversed, tx.transaction_type_enum   AS transactionType, sa.group_id  AS groupId, sa.client_id  AS clientId, sa.gsim_id  AS gsimId FROM m_savings_account_transaction tx  INNER JOIN m_savings_account sa ON tx.savings_account_id = sa.id  LEFT JOIN m_payment_detail dp ON dp.id = tx.payment_detail_id WHERE tx.id = ?1  AND sa.id = ?2", nativeQuery = true)
    RevokedInterestTransactionData findSavingsAccountTransaction(Long transactionId, Long savingsAccountId);

}
