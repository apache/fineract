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
 * Immutable data object representing disbursement information.
 */
public class DisbursementData implements Comparable<DisbursementData> {

    @SuppressWarnings("unused")
    private final Long id;
    private final LocalDate expectedDisbursementDate;
    private final LocalDate actualDisbursementDate;
    private final BigDecimal principal;
    @SuppressWarnings("unused")
    private final String loanChargeId;
    private final BigDecimal chargeAmount;
    private final BigDecimal waivedChargeAmount;

    //import fields
    private transient Integer rowIndex;
    private String dateFormat;
    private String locale;
    private String note;
    private transient String linkAccountId;

    public  static DisbursementData importInstance(LocalDate actualDisbursementDate,String linkAccountId,
            Integer rowIndex,String locale,String dateFormat){
        return new DisbursementData(actualDisbursementDate,linkAccountId,rowIndex,locale,dateFormat);
    }
    private DisbursementData(LocalDate actualDisbursementDate,String linkAccountId,
            Integer rowIndex,String locale,String dateFormat) {
        this.dateFormat= dateFormat;
        this.locale= locale;
        this.actualDisbursementDate = actualDisbursementDate;
        this.rowIndex = rowIndex;
        this.note="";
        this.linkAccountId=linkAccountId;
        this.id=null;
        this.expectedDisbursementDate=null;
        this.principal=null;
        this.loanChargeId=null;
        this.chargeAmount=null;
        this.waivedChargeAmount=null;

    }

    public String getLinkAccountId() {
        return linkAccountId;
    }

    public DisbursementData(Long id, final LocalDate expectedDisbursementDate, final LocalDate actualDisbursementDate,
            final BigDecimal principalDisbursed, final String loanChargeId, BigDecimal chargeAmount, BigDecimal waivedChargeAmount) {
        this.id = id;
        this.expectedDisbursementDate = expectedDisbursementDate;
        this.actualDisbursementDate = actualDisbursementDate;
        this.principal = principalDisbursed;
        this.loanChargeId = loanChargeId;
        this.chargeAmount = chargeAmount;
        this.waivedChargeAmount = waivedChargeAmount;
    }

    public LocalDate disbursementDate() {
        LocalDate disbursementDate = this.expectedDisbursementDate;
        if (this.actualDisbursementDate != null) {
            disbursementDate = this.actualDisbursementDate;
        }
        return disbursementDate;
    }

    public BigDecimal amount() {
        return this.principal;
    }

    public BigDecimal getChargeAmount() {
        return this.chargeAmount;
    }

    public boolean isDisbursed() {
        return this.actualDisbursementDate != null;
    }

    @Override
    public int compareTo(final DisbursementData obj) {
        if (obj == null) { return -1; }

        return obj.expectedDisbursementDate.compareTo(this.expectedDisbursementDate);
    }

    public boolean isDueForDisbursement(final LocalDate fromNotInclusive, final LocalDate upToAndInclusive) {
        final LocalDate dueDate = disbursementDate();
        return occursOnDayFromAndUpToAndIncluding(fromNotInclusive, upToAndInclusive, dueDate);
    }

    private boolean occursOnDayFromAndUpToAndIncluding(final LocalDate fromNotInclusive, final LocalDate upToAndInclusive,
            final LocalDate target) {
        return target != null && target.isAfter(fromNotInclusive) && !target.isAfter(upToAndInclusive);
    }

    
    public BigDecimal getWaivedChargeAmount() {
        if(this.waivedChargeAmount == null){
            return BigDecimal.ZERO;
        }
        return this.waivedChargeAmount;
    }

}