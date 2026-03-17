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
package org.apache.fineract.integrationtests.common.workingcapitalloanproduct;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.ObjectMapperFactory;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest;
import org.apache.fineract.client.models.PutWorkingCapitalLoanProductsProductIdRequest;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalLoanPeriodFrequencyType;

@Slf4j
public class WorkingCapitalLoanProductTestBuilder {

    private static final String DEFAULT_NAME = "Test WCP Product";
    private static final String DEFAULT_SHORT_NAME = "TWCP";
    private static final String DEFAULT_CURRENCY_CODE = "USD";
    private static final Integer DEFAULT_DECIMAL_PLACE = 2;
    private static final Integer DEFAULT_CURRENCY_IN_MULTIPLES_OF = 1;
    private static final String DEFAULT_AMORTIZATION = WorkingCapitalAmortizationType.EIR.name();
    private static final Integer DEFAULT_NPV_DAY_COUNT = 360;
    private static final BigDecimal DEFAULT_PRINCIPAL_AMOUNT = BigDecimal.valueOf(10000);
    private static final BigDecimal DEFAULT_PERIOD_PAYMENT_RATE = BigDecimal.valueOf(1.0);
    private static final Integer DEFAULT_PERIOD_PAYMENT_FREQUENCY = 30;
    private static final String DEFAULT_PERIOD_PAYMENT_FREQUENCY_TYPE = WorkingCapitalLoanPeriodFrequencyType.DAYS.name();
    private static final List<String> DEFAULT_PAYMENT_ALLOCATION_TYPES = List.of("PENALTY", "FEE", "PRINCIPAL");

    private String name = DEFAULT_NAME;
    private String shortName = DEFAULT_SHORT_NAME;
    private String description;
    private Long fundId;
    private String externalId;
    private String currencyCode = DEFAULT_CURRENCY_CODE;
    private Integer decimalPlace = DEFAULT_DECIMAL_PLACE;
    private Integer currencyInMultiplesOf = DEFAULT_CURRENCY_IN_MULTIPLES_OF;
    private String amortizationType = DEFAULT_AMORTIZATION;
    private BigDecimal flatPercentageAmount;
    private Long delinquencyBucketId;
    private Integer npvDayCount = DEFAULT_NPV_DAY_COUNT;
    private BigDecimal principalAmountMin;
    private BigDecimal principalAmountDefault = DEFAULT_PRINCIPAL_AMOUNT;
    private BigDecimal principalAmountMax;
    private BigDecimal minPeriodPaymentRate;
    private BigDecimal periodPaymentRate = DEFAULT_PERIOD_PAYMENT_RATE;
    private BigDecimal maxPeriodPaymentRate;
    private BigDecimal discount;
    private Integer repaymentEvery = DEFAULT_PERIOD_PAYMENT_FREQUENCY;
    private String repaymentFrequencyType = DEFAULT_PERIOD_PAYMENT_FREQUENCY_TYPE;
    private List<String> paymentAllocationTypes = DEFAULT_PAYMENT_ALLOCATION_TYPES;
    private Map<String, Boolean> allowAttributeOverrides;

    public WorkingCapitalLoanProductTestBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withShortName(final String shortName) {
        this.shortName = shortName;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDescription(final String description) {
        this.description = description;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withFundId(final Long fundId) {
        this.fundId = fundId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withExternalId(final String externalId) {
        this.externalId = externalId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withCurrencyCode(final String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDecimalPlace(final Integer decimalPlace) {
        this.decimalPlace = decimalPlace;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withCurrencyInMultiplesOf(final Integer currencyInMultiplesOf) {
        this.currencyInMultiplesOf = currencyInMultiplesOf;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withAmortizationType(final String amortizationType) {
        this.amortizationType = amortizationType;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withFlatPercentageAmount(final BigDecimal flatPercentageAmount) {
        this.flatPercentageAmount = flatPercentageAmount;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDelinquencyBucketId(final Long delinquencyBucketId) {
        this.delinquencyBucketId = delinquencyBucketId;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withNpvDayCount(final Integer npvDayCount) {
        this.npvDayCount = npvDayCount;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPaymentAllocationTypes(final List<String> paymentAllocationTypes) {
        this.paymentAllocationTypes = paymentAllocationTypes;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPrincipalAmountMin(final BigDecimal principalAmountMin) {
        this.principalAmountMin = principalAmountMin;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPrincipalAmountDefault(final BigDecimal principalAmountDefault) {
        this.principalAmountDefault = principalAmountDefault;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPrincipalAmountMax(final BigDecimal principalAmountMax) {
        this.principalAmountMax = principalAmountMax;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withMinPeriodPaymentRate(final BigDecimal minPeriodPaymentRate) {
        this.minPeriodPaymentRate = minPeriodPaymentRate;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withPeriodPaymentRate(final BigDecimal periodPaymentRate) {
        this.periodPaymentRate = periodPaymentRate;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withMaxPeriodPaymentRate(final BigDecimal maxPeriodPaymentRate) {
        this.maxPeriodPaymentRate = maxPeriodPaymentRate;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withDiscount(final BigDecimal discount) {
        this.discount = discount;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withRepaymentEvery(final Integer repaymentEvery) {
        this.repaymentEvery = repaymentEvery;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withRepaymentFrequencyType(final String repaymentFrequencyType) {
        this.repaymentFrequencyType = repaymentFrequencyType;
        return this;
    }

    public WorkingCapitalLoanProductTestBuilder withAllowAttributeOverrides(final Map<String, Boolean> allowAttributeOverrides) {
        this.allowAttributeOverrides = allowAttributeOverrides;
        return this;
    }

    public PostWorkingCapitalLoanProductsRequest build() {
        final PostWorkingCapitalLoanProductsRequest request = new PostWorkingCapitalLoanProductsRequest();
        populateCommonFields(request);
        setPaymentAllocation(request);
        setAllowAttributeOverrides(request);
        return request;
    }

    public PutWorkingCapitalLoanProductsProductIdRequest buildUpdateRequest() {
        final PutWorkingCapitalLoanProductsProductIdRequest request = new PutWorkingCapitalLoanProductsProductIdRequest();
        populateCommonFields(request);
        setPaymentAllocation(request);
        setAllowAttributeOverrides(request);
        return request;
    }

    private void populateCommonFields(final PostWorkingCapitalLoanProductsRequest request) {
        request.setName(this.name);
        request.setShortName(this.shortName);
        request.setDescription(this.description);
        request.setFundId(this.fundId);
        request.setExternalId(this.externalId);
        request.setCurrencyCode(this.currencyCode);
        request.setDigitsAfterDecimal(this.decimalPlace);
        request.setInMultiplesOf(this.currencyInMultiplesOf);
        if (this.amortizationType != null) {
            request.setAmortizationType(PostWorkingCapitalLoanProductsRequest.AmortizationTypeEnum.valueOf(this.amortizationType));
        }
        request.setFlatPercentageAmount(this.flatPercentageAmount);
        request.setDelinquencyBucketId(this.delinquencyBucketId);
        request.setNpvDayCount(this.npvDayCount);
        request.setMinPrincipal(this.principalAmountMin);
        request.setPrincipal(this.principalAmountDefault);
        request.setMaxPrincipal(this.principalAmountMax);
        request.setMinPeriodPaymentRate(this.minPeriodPaymentRate);
        request.setPeriodPaymentRate(this.periodPaymentRate);
        request.setMaxPeriodPaymentRate(this.maxPeriodPaymentRate);
        request.setDiscount(this.discount);
        request.setRepaymentEvery(this.repaymentEvery);
        if (this.repaymentFrequencyType != null) {
            request.setRepaymentFrequencyType(
                    PostWorkingCapitalLoanProductsRequest.RepaymentFrequencyTypeEnum.valueOf(this.repaymentFrequencyType));
        }
        request.setLocale("en_US");
        request.setDateFormat("yyyy-MM-dd");
    }

    private void populateCommonFields(final PutWorkingCapitalLoanProductsProductIdRequest request) {
        request.setName(this.name);
        request.setShortName(this.shortName);
        request.setDescription(this.description);
        request.setFundId(this.fundId);
        request.setCurrencyCode(this.currencyCode);
        request.setDigitsAfterDecimal(this.decimalPlace);
        request.setInMultiplesOf(this.currencyInMultiplesOf);
        if (this.amortizationType != null) {
            request.setAmortizationType(PutWorkingCapitalLoanProductsProductIdRequest.AmortizationTypeEnum.valueOf(this.amortizationType));
        }
        request.setFlatPercentageAmount(this.flatPercentageAmount);
        request.setDelinquencyBucketId(this.delinquencyBucketId);
        request.setNpvDayCount(this.npvDayCount);
        request.setMinPrincipal(this.principalAmountMin);
        request.setPrincipal(this.principalAmountDefault);
        request.setMaxPrincipal(this.principalAmountMax);
        request.setMinPeriodPaymentRate(this.minPeriodPaymentRate);
        request.setPeriodPaymentRate(this.periodPaymentRate);
        request.setMaxPeriodPaymentRate(this.maxPeriodPaymentRate);
        request.setDiscount(this.discount);
        request.setRepaymentEvery(this.repaymentEvery);
        if (this.repaymentFrequencyType != null) {
            request.setRepaymentFrequencyType(
                    PutWorkingCapitalLoanProductsProductIdRequest.RepaymentFrequencyTypeEnum.valueOf(this.repaymentFrequencyType));
        }
        request.setLocale("en_US");
        request.setDateFormat("yyyy-MM-dd");
    }

    private void setPaymentAllocation(final PostWorkingCapitalLoanProductsRequest request) {
        setPaymentAllocation(request, PostWorkingCapitalLoanProductsRequest.class);
    }

    private void setPaymentAllocation(final PutWorkingCapitalLoanProductsProductIdRequest request) {
        setPaymentAllocation(request, PutWorkingCapitalLoanProductsProductIdRequest.class);
    }

    private <T> void setPaymentAllocation(final T request, final Class<T> requestClass) {
        if (this.paymentAllocationTypes != null && !this.paymentAllocationTypes.isEmpty()) {
            try {
                final ObjectMapper objectMapper = ObjectMapperFactory.getShared();
                final String requestJson = objectMapper.writeValueAsString(request);
                final ObjectNode requestNode = (ObjectNode) objectMapper.readTree(requestJson);
                final ArrayNode paymentAllocationArray = objectMapper.createArrayNode();
                final ObjectNode paymentAllocationNode = objectMapper.createObjectNode();
                paymentAllocationNode.put("transactionType", "DEFAULT");
                final ArrayNode paymentAllocationOrderArray = objectMapper.createArrayNode();
                int order = 1;
                for (final String allocationType : this.paymentAllocationTypes) {
                    final ObjectNode orderItem = objectMapper.createObjectNode();
                    orderItem.put("paymentAllocationRule", allocationType);
                    orderItem.put("order", order++);
                    paymentAllocationOrderArray.add(orderItem);
                }
                paymentAllocationNode.set("paymentAllocationOrder", paymentAllocationOrderArray);
                paymentAllocationArray.add(paymentAllocationNode);
                requestNode.set("paymentAllocation", paymentAllocationArray);
                final T updatedRequest = objectMapper.treeToValue(requestNode, requestClass);
                copyAllFields(updatedRequest, request, requestClass);
            } catch (final Exception e) {
                throw new IllegalStateException("Failed to set paymentAllocation", e);
            }
        }
    }

    private void setAllowAttributeOverrides(final PostWorkingCapitalLoanProductsRequest request) {
        setAllowAttributeOverrides(request, PostWorkingCapitalLoanProductsRequest.class);
    }

    private void setAllowAttributeOverrides(final PutWorkingCapitalLoanProductsProductIdRequest request) {
        setAllowAttributeOverrides(request, PutWorkingCapitalLoanProductsProductIdRequest.class);
    }

    private <T> void setAllowAttributeOverrides(final T request, final Class<T> requestClass) {
        if (this.allowAttributeOverrides == null || this.allowAttributeOverrides.isEmpty()) {
            return;
        }

        try {
            final ObjectMapper objectMapper = ObjectMapperFactory.getShared();
            final String requestJson = objectMapper.writeValueAsString(request);
            final ObjectNode requestNode = (ObjectNode) objectMapper.readTree(requestJson);
            final ObjectNode allowOverridesNode = objectMapper.createObjectNode();
            for (final Map.Entry<String, Boolean> entry : this.allowAttributeOverrides.entrySet()) {
                allowOverridesNode.put(entry.getKey(), entry.getValue());
            }
            requestNode.set("allowAttributeOverrides", allowOverridesNode);
            final T updatedRequest = objectMapper.treeToValue(requestNode, requestClass);
            copyAllFields(updatedRequest, request, requestClass);
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to set allowAttributeOverrides", e);
        }
    }

    private <T> void copyAllFields(final T source, final T target, final Class<T> clazz) {
        final Field[] fields = clazz.getDeclaredFields();
        for (final Field field : fields) {
            try {
                if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                    continue;
                }
                field.setAccessible(true);
                final Object value = field.get(source);
                if (value != null) {
                    field.set(target, value);
                }
            } catch (final IllegalAccessException e) {
                log.warn("Failed to copy field {}: {}", field.getName(), e.getMessage());
            }
        }
    }
}
