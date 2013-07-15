/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.note.domain;

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