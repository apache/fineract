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
package org.apache.fineract.accounting.producttoaccountmapping.service;

import java.util.List;
import java.util.Map;

import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;

public interface ProductToGLAccountMappingReadPlatformService {

    public Map<String, Object> fetchAccountMappingDetailsForLoanProduct(final Long loanProductId, final Integer accountingType);

    public List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappingsForLoanProduct(final Long loanProductId);

    public List<ChargeToGLAccountMapper> fetchFeeToIncomeOrLiabilityAccountMappingsForLoanProduct(final Long loanProductId);

    public List<ChargeToGLAccountMapper> fetchPenaltyToIncomeAccountMappingsForLoanProduct(final Long loanProductId);

    public Map<String, Object> fetchAccountMappingDetailsForSavingsProduct(final Long savingsProductId, final Integer accountingType);

    public List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappingsForSavingsProduct(final Long savingsProductId);

    public List<ChargeToGLAccountMapper> fetchFeeToIncomeAccountMappingsForSavingsProduct(final Long savingsProductId);

    public List<ChargeToGLAccountMapper> fetchPenaltyToIncomeAccountMappingsForSavingsProduct(final Long savingsProductId);
    
    public Map<String, Object> fetchAccountMappingDetailsForShareProduct(final Long productId, final Integer accountingType);

    public List<PaymentTypeToGLAccountMapper> fetchPaymentTypeToFundSourceMappingsForShareProduct(final Long productId);

    public List<ChargeToGLAccountMapper> fetchFeeToIncomeAccountMappingsForShareProduct(final Long productId);

}