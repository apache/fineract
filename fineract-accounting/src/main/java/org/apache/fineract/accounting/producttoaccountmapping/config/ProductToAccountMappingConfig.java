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
package org.apache.fineract.accounting.producttoaccountmapping.config;

import org.apache.fineract.accounting.glaccount.domain.GLAccountRepository;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.apache.fineract.accounting.producttoaccountmapping.serialization.ProductToGLAccountMappingFromApiJsonDeserializer;
import org.apache.fineract.accounting.producttoaccountmapping.service.LoanProductToGLAccountMappingHelper;
import org.apache.fineract.accounting.producttoaccountmapping.service.SavingsProductToGLAccountMappingHelper;
import org.apache.fineract.accounting.producttoaccountmapping.service.ShareProductToGLAccountMappingHelper;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepository;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.paymenttype.domain.PaymentTypeRepositoryWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Product to GL Account Mapping related beans in the system.
 * <p>
 * This class is responsible for defining and configuring all necessary beans related to mapping products to GL
 * accounts, including deserializers and helper services.
 * <p>
 * All beans are annotated with {@code @ConditionalOnMissingBean} to allow for easy overriding in test configurations or
 * custom implementations.
 *
 * @see org.apache.fineract.accounting.producttoaccountmapping.serialization.ProductToGLAccountMappingFromApiJsonDeserializer
 * @see org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingHelper
 * @see org.apache.fineract.accounting.producttoaccountmapping.service.SavingsProductToGLAccountMappingHelper
 * @see org.apache.fineract.accounting.producttoaccountmapping.service.ShareProductToGLAccountMappingHelper
 */
@Configuration
public class ProductToAccountMappingConfig {

    /**
     * Creates and configures the {@code ProductToGLAccountMappingFromApiJsonDeserializer} bean.
     *
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @return a fully configured {@code ProductToGLAccountMappingFromApiJsonDeserializer} instance
     * @see org.apache.fineract.accounting.producttoaccountmapping.serialization.ProductToGLAccountMappingFromApiJsonDeserializer
     */
    @Bean
    @ConditionalOnMissingBean
    public ProductToGLAccountMappingFromApiJsonDeserializer productToGLAccountMappingFromApiJsonDeserializer(
            final FromJsonHelper fromApiJsonHelper) {
        return new ProductToGLAccountMappingFromApiJsonDeserializer(fromApiJsonHelper);
    }

    /**
     * Creates and configures the {@code SavingsProductToGLAccountMappingHelper} bean.
     *
     * @param glAccountRepository
     *            the GL account repository
     * @param glAccountMappingRepository
     *            the product to GL account mapping repository
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @param chargeRepositoryWrapper
     *            the charge repository wrapper
     * @param accountRepositoryWrapper
     *            the GL account repository wrapper
     * @param paymentTypeRepositoryWrapper
     *            the payment type repository wrapper
     * @param codeValueRepository
     *            the code value repository
     * @return a fully configured {@code SavingsProductToGLAccountMappingHelper} instance
     * @see org.apache.fineract.accounting.producttoaccountmapping.service.SavingsProductToGLAccountMappingHelper
     */
    @Bean
    @Qualifier("savingsProductToGLAccountMappingHelper")
    @ConditionalOnMissingBean
    public SavingsProductToGLAccountMappingHelper savingsProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final GLAccountRepositoryWrapper accountRepositoryWrapper,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper, final CodeValueRepository codeValueRepository) {

        return new SavingsProductToGLAccountMappingHelper(glAccountRepository, glAccountMappingRepository, fromApiJsonHelper,
                chargeRepositoryWrapper, accountRepositoryWrapper, paymentTypeRepositoryWrapper, codeValueRepository);
    }

    /**
     * Creates and configures the {@code ShareProductToGLAccountMappingHelper} bean.
     *
     * @param glAccountRepository
     *            the GL account repository
     * @param glAccountMappingRepository
     *            the product to GL account mapping repository
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @param chargeRepositoryWrapper
     *            the charge repository wrapper
     * @param accountRepositoryWrapper
     *            the GL account repository wrapper
     * @param paymentTypeRepositoryWrapper
     *            the payment type repository wrapper
     * @param codeValueRepository
     *            the code value repository
     * @return a fully configured {@code ShareProductToGLAccountMappingHelper} instance
     * @see org.apache.fineract.accounting.producttoaccountmapping.service.ShareProductToGLAccountMappingHelper
     */
    @Bean
    @Qualifier("shareProductToGLAccountMappingHelper")
    @ConditionalOnMissingBean
    public ShareProductToGLAccountMappingHelper shareProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final GLAccountRepositoryWrapper accountRepositoryWrapper,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper, final CodeValueRepository codeValueRepository) {

        return new ShareProductToGLAccountMappingHelper(glAccountRepository, glAccountMappingRepository, fromApiJsonHelper,
                chargeRepositoryWrapper, accountRepositoryWrapper, paymentTypeRepositoryWrapper, codeValueRepository);
    }

    /**
     * Creates and configures the {@code ShareProductToGLAccountMappingHelper} bean.
     *
     * @param glAccountRepository
     *            the GL account repository
     * @param glAccountMappingRepository
     *            the product to GL account mapping repository
     * @param fromApiJsonHelper
     *            the helper class for JSON processing
     * @param chargeRepositoryWrapper
     *            the charge repository wrapper
     * @param accountRepositoryWrapper
     *            the GL account repository wrapper
     * @param paymentTypeRepositoryWrapper
     *            the payment type repository wrapper
     * @param codeValueRepository
     *            the code value repository
     * @return a fully configured {@code ShareProductToGLAccountMappingHelper} instance
     * @see org.apache.fineract.accounting.producttoaccountmapping.service.ShareProductToGLAccountMappingHelper
     */
    @Bean
    @Qualifier("loanProductToGLAccountMappingHelper")
    @ConditionalOnMissingBean
    public LoanProductToGLAccountMappingHelper loanProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final ChargeRepositoryWrapper chargeRepositoryWrapper, final GLAccountRepositoryWrapper accountRepositoryWrapper,
            final PaymentTypeRepositoryWrapper paymentTypeRepositoryWrapper, final CodeValueRepository codeValueRepository) {

        return new LoanProductToGLAccountMappingHelper(glAccountRepository, glAccountMappingRepository, fromApiJsonHelper,
                chargeRepositoryWrapper, accountRepositoryWrapper, paymentTypeRepositoryWrapper, codeValueRepository);
    }
}
