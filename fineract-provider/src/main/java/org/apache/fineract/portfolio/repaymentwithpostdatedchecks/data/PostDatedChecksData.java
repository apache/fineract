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
package org.apache.fineract.portfolio.repaymentwithpostdatedchecks.data;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class PostDatedChecksData {

    private final Long id;

    private final Integer installmentId;

    private final String name;

    private final Long accountNo;

    private final BigDecimal amount;

    private final LocalDate installmentDate;

    private final Long checkNo;

    private final Integer status;

    private PostDatedChecksData(final LocalDate installmentDate, final Long id, final Integer installmentId, final Long accountNo,
            final BigDecimal amount, final String name, final Long checkNo, final Integer status) {
        this.accountNo = accountNo;
        this.amount = amount;
        this.name = name;
        this.id = id;
        this.installmentDate = installmentDate;
        this.installmentId = installmentId;
        this.checkNo = checkNo;
        this.status = status;
    }

    public static PostDatedChecksData from(final LocalDate installmentDate, final Long id, final Integer installmentId,
            final Long accountNo, final BigDecimal amount, final String name, final Long checkNo, final Integer status) {
        return new PostDatedChecksData(installmentDate, id, installmentId, accountNo, amount, name, checkNo, status);
    }

}
