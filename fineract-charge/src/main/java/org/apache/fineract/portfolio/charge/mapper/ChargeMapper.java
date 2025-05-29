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
package org.apache.fineract.portfolio.charge.mapper;

import java.time.MonthDay;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Optional;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.core.config.MapstructMapperConfig;
import org.apache.fineract.infrastructure.core.exception.PlatformDataIntegrityException;
import org.apache.fineract.portfolio.charge.data.CreateChargeRequest;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentType;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupRepositoryWrapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(config = MapstructMapperConfig.class)
public abstract class ChargeMapper {

    @Autowired
    protected GLAccountRepositoryWrapper glAccountRepository;

    @Autowired
    protected TaxGroupRepositoryWrapper taxGroupRepository;

    @Autowired
    protected PaymentTypeRepositoryWrapper paymentTyperepositoryWrapper;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "taxGroup", expression = "java(mapTaxGroup(request.getTaxGroupId()))")
    @Mapping(target = "account", expression = "java(mapGLAccount(request.getIncomeAccountId()))")
    @Mapping(target = "feeOnDay", expression = "java(extractDayFromMonthDay(request.getFeeOnMonthDay(), request.getMonthDayFormat(), request.getLocale()))")
    @Mapping(target = "feeOnMonth", expression = "java(extractMonthFromMonthDay(request.getFeeOnMonthDay(), request.getMonthDayFormat(), request.getLocale()))")
    @Mapping(target = "chargeCalculation", source = "chargeCalculationType")
    @Mapping(target = "chargeTimeType", source = "chargeTimeType")
    @Mapping(target = "chargePaymentMode", source = "chargePaymentMode")
    @Mapping(target = "chargeAppliesTo", source = "chargeAppliesTo")
    @Mapping(target = "feeInterval", source = "feeInterval")
    @Mapping(target = "feeFrequency", source = "feeFrequency")
    @Mapping(target = "paymentType", expression = "java(mapPaymentType(request.getEnablePaymentType(), request.getPaymentTypeId()))")
    @Mapping(target = "enableFreeWithdrawal", source = "enableFreeWithdrawalCharge")
    @Mapping(target = "restartFrequency", source = "restartCountFrequency")
    @Mapping(target = "restartFrequencyEnum", source = "countFrequencyType")
    public abstract Charge map(CreateChargeRequest request);

    @Named("mapTaxGroup")
    protected TaxGroup mapTaxGroup(Long taxGroupId) {
        return Optional.ofNullable(taxGroupId).map(taxGroupRepository::findOneWithNotFoundDetection).orElse(null);
    }

    @Named("mapGLAccount")
    protected GLAccount mapGLAccount(Long accountId) {
        return Optional.ofNullable(accountId).map(glAccountRepository::findOneWithNotFoundDetection).orElse(null);
    }

    @Named("mapPaymentType")
    protected PaymentType mapPaymentType(Boolean enablePaymentType, Long paymentTypeId) {
        if (Boolean.TRUE.equals(enablePaymentType) && paymentTypeId != null) {
            return paymentTyperepositoryWrapper.findOneWithNotFoundDetection(paymentTypeId);
        }
        return null;
    }

    @Named("extractDayFromMonthDay")
    protected Integer extractDayFromMonthDay(String monthDay, String dateFormat, String locale) {
        if (StringUtils.isBlank(monthDay)) {
            return null;
        }
        try {
            final Locale defaultLocale = Optional.ofNullable(locale).map(LocaleUtils::toLocale).orElse(Locale.getDefault());
            final DateTimeFormatter formatterDate = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                    .appendPattern(dateFormat).toFormatter(defaultLocale).withResolverStyle(ResolverStyle.STRICT);
            return MonthDay.parse(monthDay, formatterDate).getDayOfMonth();
        } catch (DateTimeParseException e) {
            throw new PlatformDataIntegrityException("",
                    "Can not get month DAY value by dateFormat: " + dateFormat + " from time " + monthDay, e);
        }
    }

    @Named("extractMonthFromMonthDay")
    protected Integer extractMonthFromMonthDay(String monthDay, String dateFormat, String locale) {
        if (StringUtils.isBlank(monthDay)) {
            return null;
        }
        try {
            final Locale defaultLocale = Optional.ofNullable(locale).map(LocaleUtils::toLocale).orElse(Locale.getDefault());
            final DateTimeFormatter formatterDate = new DateTimeFormatterBuilder().parseCaseInsensitive().parseLenient()
                    .appendPattern(dateFormat).toFormatter(defaultLocale).withResolverStyle(ResolverStyle.STRICT);
            return MonthDay.parse(monthDay, formatterDate).getMonthValue();
        } catch (DateTimeParseException e) {
            throw new PlatformDataIntegrityException("", "Can not get month value by dateFormat: " + dateFormat + " from time " + monthDay,
                    e);
        }
    }
}
