package org.mifosplatform.accounting.producttoaccountmapping.service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.mifosplatform.accounting.common.AccountingConstants.CASH_ACCOUNTS_FOR_LOAN;
import org.mifosplatform.accounting.common.AccountingConstants.LOAN_PRODUCT_ACCOUNTING_PARAMS;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.accounting.glaccount.domain.GLAccountRepository;
import org.mifosplatform.accounting.glaccount.domain.GLAccountType;
import org.mifosplatform.accounting.glaccount.exception.GLAccountNotFoundException;
import org.mifosplatform.accounting.producttoaccountmapping.domain.PortfolioProductType;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMapping;
import org.mifosplatform.accounting.producttoaccountmapping.domain.ProductToGLAccountMappingRepository;
import org.mifosplatform.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingInvalidException;
import org.mifosplatform.accounting.producttoaccountmapping.exception.ProductToGLAccountMappingNotFoundException;
import org.mifosplatform.infrastructure.codes.domain.CodeValue;
import org.mifosplatform.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.portfolio.paymentdetail.PaymentDetailConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@Component
public class ProductToGLAccountMappingHelper {

    protected final GLAccountRepository accountRepository;
    protected final ProductToGLAccountMappingRepository accountMappingRepository;
    protected final FromJsonHelper fromApiJsonHelper;
    private final CodeValueRepositoryWrapper codeValueRepositoryWrapper;

    @Autowired
    public ProductToGLAccountMappingHelper(final GLAccountRepository glAccountRepository,
            final ProductToGLAccountMappingRepository glAccountMappingRepository, final FromJsonHelper fromApiJsonHelper,
            final CodeValueRepositoryWrapper codeValueRepositoryWrapper) {
        this.accountRepository = glAccountRepository;
        this.accountMappingRepository = glAccountMappingRepository;
        this.fromApiJsonHelper = fromApiJsonHelper;
        this.codeValueRepositoryWrapper = codeValueRepositoryWrapper;

    }

    public void saveProductToAccountMapping(final JsonElement element, final String paramName, final Long productId,
            final int placeHolderTypeId, final GLAccountType expectedAccountType, final PortfolioProductType portfolioProductType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);
        final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);

        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                portfolioProductType.getValue(), placeHolderTypeId);
        this.accountMappingRepository.save(accountMapping);
    }

    public void mergeProductToAccountMappingChanges(final JsonElement element, final String paramName, final Long productId,
            final int accountTypeId, final String accountTypeName, final Map<String, Object> changes,
            final GLAccountType expectedAccountType, final PortfolioProductType portfolioProductType) {
        final Long accountId = this.fromApiJsonHelper.extractLongNamed(paramName, element);

        // get the existing product
        if (accountId != null) {
            final ProductToGLAccountMapping accountMapping = this.accountMappingRepository.findCoreProductToFinAccountMapping(productId,
                    portfolioProductType.getValue(), accountTypeId);
            if (accountMapping == null) { throw new ProductToGLAccountMappingNotFoundException(portfolioProductType, productId,
                    accountTypeName); }
            if (accountMapping.getGlAccount().getId() != accountId) {
                final GLAccount glAccount = getAccountByIdAndType(paramName, expectedAccountType, accountId);
                changes.put(paramName, accountId);
                accountMapping.setGlAccount(glAccount);
                this.accountMappingRepository.save(accountMapping);
            }
        }
    }

    /**
     * Saves the payment type to Fund source mappings for a particular
     * product/product type (also populates the changes array if passed in)
     * 
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    public void savePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            Map<String, Object> changes, final PortfolioProductType portfolioProductType) {
        JsonArray paymentChannelMappingArray = fromApiJsonHelper.extractJsonArrayNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
        if (paymentChannelMappingArray != null) {
            if (changes != null) {
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
                        command.jsonFragment(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue()));
            }
            for (int i = 0; i < paymentChannelMappingArray.size(); i++) {
                final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                final Long paymentTypeId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_TYPE.getValue()).getAsLong();
                final Long paymentSpecificFundAccountId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).getAsLong();
                savePaymentChannelToFundSourceMapping(productId, paymentTypeId, paymentSpecificFundAccountId, portfolioProductType);
            }
        }
    }

    /**
     * @param command
     * @param element
     * @param productId
     * @param changes
     */
    public void updatePaymentChannelToFundSourceMappings(final JsonCommand command, final JsonElement element, final Long productId,
            Map<String, Object> changes, final PortfolioProductType portfolioProductType) {
        // find all existing payment Channel to Fund source Mappings
        List<ProductToGLAccountMapping> existingPaymentChannelToFundSourceMappings = accountMappingRepository
                .findAllPaymentTypeToFundSourceMappings(productId, portfolioProductType.getValue());
        JsonArray paymentChannelMappingArray = fromApiJsonHelper.extractJsonArrayNamed(
                LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(), element);
        /**
         * Variable stores a map representation of Payment channels (key) and
         * their fund sources (value) extracted from the passed in Jsoncommand
         **/
        Map<Long, Long> inputPaymentChannelFundSourceMap = new HashMap<Long, Long>();
        /***
         * Variable stores all payment types which have already been mapped to
         * Fund Sources in the system
         **/
        Set<Long> existingPaymentTypes = new HashSet<Long>();
        if (paymentChannelMappingArray != null) {
            if (changes != null) {
                changes.put(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue(),
                        command.jsonFragment(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_CHANNEL_FUND_SOURCE_MAPPING.getValue()));
            }

            for (int i = 0; i < paymentChannelMappingArray.size(); i++) {
                final JsonObject jsonObject = paymentChannelMappingArray.get(i).getAsJsonObject();
                final Long paymentTypeId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.PAYMENT_TYPE.getValue()).getAsLong();
                final Long paymentSpecificFundAccountId = jsonObject.get(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue()).getAsLong();
                inputPaymentChannelFundSourceMap.put(paymentTypeId, paymentSpecificFundAccountId);
            }

            // If input map is empty, delete all existing mappings
            if (inputPaymentChannelFundSourceMap.size() == 0) {
                accountMappingRepository.deleteInBatch(existingPaymentChannelToFundSourceMappings);
            }/**
             * Else, <br/>
             * update existing mappings OR <br/>
             * delete old mappings (which re already present, but not passed in
             * as a part of Jsoncommand)<br/>
             * Create new mappings for payment types that are passed in as a
             * part of the Jsoncommand but not already present
             * 
             **/
            else {
                for (ProductToGLAccountMapping existingPaymentChannelToFundSourceMapping : existingPaymentChannelToFundSourceMappings) {
                    Long currentPaymentChannelId = existingPaymentChannelToFundSourceMapping.getPaymentType().getId();
                    existingPaymentTypes.add(currentPaymentChannelId);
                    // update existing mappings (if required)
                    if (inputPaymentChannelFundSourceMap.containsKey(currentPaymentChannelId)) {
                        Long newGLAccountId = inputPaymentChannelFundSourceMap.get(currentPaymentChannelId);
                        if (newGLAccountId != existingPaymentChannelToFundSourceMapping.getGlAccount().getId()) {
                            final GLAccount glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(),
                                    GLAccountType.ASSET, newGLAccountId);
                            existingPaymentChannelToFundSourceMapping.setGlAccount(glAccount);
                            accountMappingRepository.save(existingPaymentChannelToFundSourceMapping);
                        }
                    }// deleted payment type
                    else {
                        accountMappingRepository.delete(existingPaymentChannelToFundSourceMapping);
                    }
                }
                // create new mappings
                Set<Long> incomingPaymentTypes = inputPaymentChannelFundSourceMap.keySet();
                incomingPaymentTypes.removeAll(existingPaymentTypes);
                // incomingPaymentTypes now only contains the newly added
                // payment Type mappings
                for (Long newPaymentType : incomingPaymentTypes) {
                    Long newGLAccountId = inputPaymentChannelFundSourceMap.get(newPaymentType);
                    savePaymentChannelToFundSourceMapping(productId, newPaymentType, newGLAccountId, portfolioProductType);
                }
            }
        }
    }

    /**
     * @param productId
     * @param jsonObject
     */
    private void savePaymentChannelToFundSourceMapping(final Long productId, final Long paymentTypeId,
            final Long paymentTypeSpecificFundAccountId, final PortfolioProductType portfolioProductType) {
        CodeValue paymentType = codeValueRepositoryWrapper.findOneByCodeNameAndIdWithNotFoundDetection(
                PaymentDetailConstants.paymentTypeCodeName, paymentTypeId);
        final GLAccount glAccount = getAccountByIdAndType(LOAN_PRODUCT_ACCOUNTING_PARAMS.FUND_SOURCE.getValue(), GLAccountType.ASSET,
                paymentTypeSpecificFundAccountId);
        final ProductToGLAccountMapping accountMapping = new ProductToGLAccountMapping(glAccount, productId,
                portfolioProductType.getValue(), CASH_ACCOUNTS_FOR_LOAN.FUND_SOURCE.getValue(), paymentType);
        this.accountMappingRepository.save(accountMapping);
    }

    /**
     * Fetches account with a particular Id and throws and Exception it is not
     * of the expected Account Category ('ASSET','liability' etc)
     * 
     * @param paramName
     * @param expectedAccountType
     * @param accountId
     * @return
     */
    public GLAccount getAccountByIdAndType(final String paramName, final GLAccountType expectedAccountType, final Long accountId) {
        final GLAccount glAccount = this.accountRepository.findOne(accountId);
        if (glAccount == null) { throw new GLAccountNotFoundException(accountId); }

        // validate account is of the expected Type
        if (glAccount.getType().intValue() != expectedAccountType.getValue()) { throw new ProductToGLAccountMappingInvalidException(
                paramName, glAccount.getName(), accountId, GLAccountType.fromInt(glAccount.getType()).toString(),
                expectedAccountType.toString()); }
        return glAccount;
    }

    public void deleteProductToGLAccountMapping(final Long loanProductId, final PortfolioProductType portfolioProductType) {
        final List<ProductToGLAccountMapping> productToGLAccountMappings = this.accountMappingRepository.findByProductIdAndProductType(
                loanProductId, portfolioProductType.getValue());
        if (productToGLAccountMappings != null && productToGLAccountMappings.size() > 0) {
            this.accountMappingRepository.deleteInBatch(productToGLAccountMappings);
        }
    }
}
