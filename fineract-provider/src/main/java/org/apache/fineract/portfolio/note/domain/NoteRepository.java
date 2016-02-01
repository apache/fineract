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
package org.apache.fineract.portfolio.note.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {

    List<Note> findByLoanId(Long id);

    List<Note> findByClientId(Long id);

    List<Note> findByGroupId(Long groupId);

    Note findByLoanIdAndId(Long loanId, Long id);

    Note findByClientIdAndId(Long clientId, Long id);

    Note findByGroupIdAndId(Long groupId, Long id);

    Note findByLoanTransactionIdAndId(Long loanTransactionId, Long id);

    List<Note> findBySavingsAccountId(Long savingAccountId);

    // Note findBySavingsAccountIdAndId(Long savingAccountId, Long id);
}