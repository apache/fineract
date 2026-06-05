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
package org.apache.fineract.cob.loan;

import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.AbstractAccountItemReader;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.loanaccount.exception.LoanNotFoundException;
import org.springframework.data.repository.CrudRepository;

/**
 * Loan-flavoured {@link AbstractAccountItemReader} that raises a {@link LoanNotFoundException} when a loan id cannot be
 * resolved. Shared by the loan and working-capital-loan COB readers.
 */
@Slf4j
public abstract class AbstractLoanItemReader<T extends AbstractPersistableCustom<Long>> extends AbstractAccountItemReader<T> {

    protected AbstractLoanItemReader(CrudRepository<T, Long> loanRepository) {
        super(loanRepository);
    }

    @Override
    protected RuntimeException notFound(Long loanId) {
        return new LoanNotFoundException(loanId);
    }

}
