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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing a loan transaction.
 */
public class LoanApprovalData {

    private final LocalDate approvalDate;
    private final BigDecimal approvalAmount;

    //import fields
    private LocalDate approvedOnDate;
    private String note;
    private String dateFormat;
    private String locale;
    private transient Integer rowIndex;

    public static LoanApprovalData importInstance(LocalDate approvedOnDate, Integer rowIndex,
            String locale,String dateFormat){
        return new LoanApprovalData(approvedOnDate,rowIndex,locale,dateFormat);
    }
    private LoanApprovalData(LocalDate approvedOnDate, Integer rowIndex,String locale,String dateFormat) {
        this.approvedOnDate = approvedOnDate;
        this.rowIndex = rowIndex;
        this.dateFormat=dateFormat;
        this.locale= locale;
        this.note="";
        this.approvalAmount=null;
        this.approvalDate=null;
    }

    public LoanApprovalData(final BigDecimal approvalAmount, final LocalDate approvalDate) {
        this.approvalDate = approvalDate;
        this.approvalAmount = approvalAmount;
    }

    public LocalDate getApprovalDate() {
        return this.approvalDate;
    }

    public BigDecimal getApprovalAmount() {
        return this.approvalAmount;
    }

}