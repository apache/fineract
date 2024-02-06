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
package org.apache.fineract.portfolio.search.data;

import static org.apache.fineract.portfolio.search.service.SearchUtil.DEFAULT_PAGE_SIZE;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Locale;
import lombok.Getter;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.logging.log4j.util.Strings;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Getter
public class TransactionSearchRequest {

    private Long accountId;
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDate fromSubmittedDate;
    private LocalDate toSubmittedDate;
    private BigDecimal fromAmount;
    private BigDecimal toAmount;
    private String[] types;
    private Boolean credit;
    private Boolean debit;

    private PageRequest pageable;

    public TransactionSearchRequest accountId(Long accountId) {
        this.accountId = accountId;
        return this;
    }

    public TransactionSearchRequest fromDate(String fromDate, String dateFormat, Locale locale) {
        this.fromDate = fromDate == null ? null : DateUtils.parseLocalDate(fromDate, dateFormat, locale);
        return this;
    }

    public TransactionSearchRequest toDate(String toDate, String dateFormat, Locale locale) {
        this.toDate = toDate == null ? null : DateUtils.parseLocalDate(toDate, dateFormat, locale);
        return this;
    }

    public TransactionSearchRequest fromSubmittedDate(String fromSubmittedDate, String dateFormat, Locale locale) {
        this.fromSubmittedDate = fromSubmittedDate == null ? null : DateUtils.parseLocalDate(fromSubmittedDate, dateFormat, locale);
        return this;
    }

    public TransactionSearchRequest toSubmittedDate(String toSubmittedDate, String dateFormat, Locale locale) {
        this.toSubmittedDate = toSubmittedDate == null ? null : DateUtils.parseLocalDate(toSubmittedDate, dateFormat, locale);
        return this;
    }

    public TransactionSearchRequest fromAmount(BigDecimal fromAmount) {
        this.fromAmount = fromAmount;
        return this;
    }

    public TransactionSearchRequest toAmount(BigDecimal toAmount) {
        this.toAmount = toAmount;
        return this;
    }

    public TransactionSearchRequest types(String types) {
        this.types = types == null ? null : types.split(",");
        return this;
    }

    public TransactionSearchRequest credit(Boolean credit) {
        this.credit = credit;
        return this;
    }

    public TransactionSearchRequest debit(Boolean debit) {
        this.debit = debit;
        return this;
    }

    public TransactionSearchRequest pageable(Integer offset, Integer limit, String orderByProps, Sort.Direction direction) {
        offset = MathUtil.nullToDefault(offset, 0);
        limit = MathUtil.nullToDefault(limit, DEFAULT_PAGE_SIZE);
        String[] properties = Strings.isEmpty(orderByProps) ? null : orderByProps.split(",");
        this.pageable = properties == null ? PageRequest.of(offset, limit) : PageRequest.of(offset, limit, direction, properties);
        return this;
    }
}
