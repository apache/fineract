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
package org.apache.fineract.integrationtests.investor.externalassetowner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.ExternalTransferLoanProductAttributesData;
import org.apache.fineract.client.models.ExternalTransferLoanProductAttributesTemplateData;
import org.apache.fineract.client.models.PageExternalTransferLoanProductAttributesData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignExternalAssetOwnerHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.junit.jupiter.api.Test;

public class ExternalAssetOwnerLoanProductAttributesTest extends FeignLoanTestBase {

    private static final String EXCLUDED_TRANSACTION_TYPES = "EXCLUDED_TRANSACTION_TYPES";
    private static final String SETTLEMENT_MODEL = "SETTLEMENT_MODEL";
    private static final String BUY_DOWN_FEE_TYPES = "BUY_DOWN_FEE,BUY_DOWN_FEE_ADJUSTMENT,BUY_DOWN_FEE_AMORTIZATION,BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT";

    private final FeignExternalAssetOwnerHelper externalAssetOwnerHelper = new FeignExternalAssetOwnerHelper(
            FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testExcludedTransactionTypesAttributeCreateUpdateDeleteLifecycle() {
        final Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

        // create
        final Long createdProductId = externalAssetOwnerHelper
                .createLoanProductAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES, BUY_DOWN_FEE_TYPES).getResourceId();
        assertEquals(loanProductId, createdProductId);

        // read back, verbatim
        final ExternalTransferLoanProductAttributesData created = retrieveSingleAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES);
        assertNotNull(created.getAttributeId());
        assertEquals(loanProductId, created.getLoanProductId());
        assertEquals(EXCLUDED_TRANSACTION_TYPES, created.getAttributeKey());
        assertEquals(BUY_DOWN_FEE_TYPES, created.getAttributeValue());

        // update, with padding around the separator that must be normalised
        final Long attributeId = created.getAttributeId();
        externalAssetOwnerHelper.updateLoanProductAttribute(loanProductId, attributeId, EXCLUDED_TRANSACTION_TYPES,
                "BUY_DOWN_FEE , BUY_DOWN_FEE_AMORTIZATION");

        final ExternalTransferLoanProductAttributesData updated = retrieveSingleAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES);
        assertEquals(attributeId, updated.getAttributeId());
        assertEquals("BUY_DOWN_FEE,BUY_DOWN_FEE_AMORTIZATION", updated.getAttributeValue());

        // delete, the product falls back to excluding nothing
        final Long deletedProductId = externalAssetOwnerHelper.deleteLoanProductAttribute(loanProductId, attributeId).getResourceId();
        assertEquals(loanProductId, deletedProductId);

        final PageExternalTransferLoanProductAttributesData afterDelete = externalAssetOwnerHelper
                .retrieveLoanProductAttributes(loanProductId, EXCLUDED_TRANSACTION_TYPES);
        assertEquals(0, afterDelete.getTotalFilteredRecords());

        // the key can be configured again afterwards
        externalAssetOwnerHelper.createLoanProductAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES, "BUY_DOWN_FEE");
        assertEquals("BUY_DOWN_FEE", retrieveSingleAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES).getAttributeValue());
    }

    @Test
    public void testTemplateContainsExcludedTransactionTypesAndSettlementModel() {
        final List<ExternalTransferLoanProductAttributesTemplateData> template = externalAssetOwnerHelper
                .retrieveLoanProductAttributesTemplate();

        final ExternalTransferLoanProductAttributesTemplateData excludedTransactionTypes = template.stream()
                .filter(attribute -> EXCLUDED_TRANSACTION_TYPES.equals(attribute.getAttributeKey())).findFirst().orElseThrow();
        assertEquals(Boolean.TRUE, excludedTransactionTypes.getMultiValue());
        assertNotNull(excludedTransactionTypes.getAttributeValues());

        assertTrue(excludedTransactionTypes.getAttributeValues().contains("BUY_DOWN_FEE"));
        assertTrue(excludedTransactionTypes.getAttributeValues().contains("BUY_DOWN_FEE_AMORTIZATION_ADJUSTMENT"));
        assertFalse(excludedTransactionTypes.getAttributeValues().contains("INVALID"));

        final ExternalTransferLoanProductAttributesTemplateData settlementModel = template.stream()
                .filter(attribute -> SETTLEMENT_MODEL.equals(attribute.getAttributeKey())).findFirst().orElseThrow();
        assertEquals(Boolean.FALSE, settlementModel.getMultiValue());
        assertEquals(List.of("DEFAULT_SETTLEMENT", "DELAYED_SETTLEMENT"), settlementModel.getAttributeValues());
    }

    @Test
    public void testCreateExcludedTransactionTypesAttributeWithUnknownTransactionTypeIsRejected() {
        final Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class, () -> externalAssetOwnerHelper
                .createLoanProductAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES, "BUY_DOWN_FEE,NOT_A_TYPE"));
        assertTrue(exception.getMessage().contains("error.msg.externalAssetOwnerLoanProductAttribute.invalidAttributeValue"));

        final PageExternalTransferLoanProductAttributesData attributes = externalAssetOwnerHelper
                .retrieveLoanProductAttributes(loanProductId, EXCLUDED_TRANSACTION_TYPES);
        assertEquals(0, attributes.getTotalFilteredRecords());
    }

    @Test
    public void testDeleteExcludedTransactionTypesAttributeOfAnotherLoanProductIsRejected() {
        final Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());
        final Long otherLoanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct());

        externalAssetOwnerHelper.createLoanProductAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES, "BUY_DOWN_FEE");
        final Long attributeId = retrieveSingleAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES).getAttributeId();

        final CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> externalAssetOwnerHelper.deleteLoanProductAttribute(otherLoanProductId, attributeId));
        assertTrue(exception.getMessage().contains("error.msg.externalAssetOwnerLoanProductAttributes.general"));

        // the attribute of the original loan product is untouched
        assertEquals("BUY_DOWN_FEE", retrieveSingleAttribute(loanProductId, EXCLUDED_TRANSACTION_TYPES).getAttributeValue());
    }

    private ExternalTransferLoanProductAttributesData retrieveSingleAttribute(final Long loanProductId, final String attributeKey) {
        final PageExternalTransferLoanProductAttributesData attributes = externalAssetOwnerHelper
                .retrieveLoanProductAttributes(loanProductId, attributeKey);
        assertEquals(1, attributes.getTotalFilteredRecords());
        return attributes.getPageItems().getFirst();
    }
}
