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
package org.apache.fineract.infrastructure.bulkimport.populator;

import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductMarketPriceData;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.math.BigDecimal;
import java.util.*;

public class SharedProductsSheetPopulator extends AbstractWorkbookPopulator {
    private List<ShareProductData> sharedProductDataList;
    private List<ChargeData> chargesForSharedProducts;
    private Map<Long,Integer[]>productToBeginEndIndexesofCharges;

    private static final int PRODUCT_ID=0;
    private static final int PRODUCT_NAME_COL = 1;
    private static final int CURRENCY_COL = 2;
    private static final int DECIMAL_PLACES_COL = 3;
    private static final int TODAYS_PRICE_COL = 4;
    private static final int CURRENCY_IN_MULTIPLES_COL = 5;
    private static final int CHARGES_ID_1_COL = 7;
    private static final int CHARGES_NAME_1_COL = 8;
    private static final int CHARGES_ID_2_COL = 9;
    private static final int CHARGES_NAME_2_COL = 10;
    private static final int CHARGES_ID_3_COL = 11;
    private static final int CHARGES_NAME_3_COL = 12;



    public SharedProductsSheetPopulator(List<ShareProductData> shareProductDataList,List<ChargeData> chargesForShares) {
        this.sharedProductDataList=shareProductDataList;
        this.chargesForSharedProducts=chargesForShares;
        
    }

    @Override
    public void populate(Workbook workbook,String dateFormat) {
        Sheet sharedProductsSheet=workbook.createSheet(TemplatePopulateImportConstants.SHARED_PRODUCTS_SHEET_NAME);
        setLayout(sharedProductsSheet);
        populateSheet(sharedProductsSheet);
        sharedProductsSheet.protectSheet("");
    }

    private void populateSheet(Sheet sharedProductsSheet) {
        int index = 0;
        int startIndexCharges = 1;
        int endIndexCharges = 0;
        productToBeginEndIndexesofCharges = new HashMap<>();
            for (ShareProductData productData : sharedProductDataList) {
                Row row = sharedProductsSheet.createRow(++index);
                writeLong(PRODUCT_ID, row, productData.getId());
                writeString(PRODUCT_NAME_COL, row, productData.getName().replaceAll("[ ]", "_"));
                writeString(CURRENCY_COL, row, productData.getCurrency().getName().replaceAll("[ ]", "_"));
                writeInt(DECIMAL_PLACES_COL, row, productData.getCurrency().decimalPlaces());
                writeBigDecimal(TODAYS_PRICE_COL, row, deriveMarketPrice(productData));
                writeInt(CURRENCY_IN_MULTIPLES_COL,row,productData.getCurrency().currencyInMultiplesOf());
                if (chargesForSharedProducts != null) {
                    int chargeRowIndex=0;
                    for (ChargeData chargeData:chargesForSharedProducts) {
                        if (chargeData.getCurrency().getName().equals(productData.getCurrency().getName())) {
                            writeString(CHARGES_NAME_1_COL+chargeRowIndex, row, chargeData.getName());
                            writeLong(CHARGES_ID_1_COL+chargeRowIndex, row, chargeData.getId());
                            chargeRowIndex+=2;
                            endIndexCharges++;
                        }
                    }
                    productToBeginEndIndexesofCharges.put(productData.getId(), new Integer[]{startIndexCharges, endIndexCharges});
                    startIndexCharges = endIndexCharges + 1;
                }
            }
    }
    private BigDecimal deriveMarketPrice(final ShareProductData shareProductData) {
        BigDecimal marketValue = shareProductData.getUnitPrice();
        Collection<ShareProductMarketPriceData> marketDataSet = shareProductData.getMarketPrice();
        if (marketDataSet != null && !marketDataSet.isEmpty()) {
            Date currentDate = DateUtils.getDateOfTenant();
            for (ShareProductMarketPriceData data : marketDataSet) {
                Date futureDate = data.getStartDate();
                if (currentDate.after(futureDate)) {
                    marketValue = data.getShareValue();
                }
            }
        }
        return marketValue;
    }

    private void setLayout(Sheet workSheet) {
        Row rowHeader=workSheet.createRow(TemplatePopulateImportConstants.ROWHEADER_INDEX);
        rowHeader.setHeight(TemplatePopulateImportConstants.ROW_HEADER_HEIGHT);

        workSheet.setColumnWidth(PRODUCT_ID,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(PRODUCT_ID,rowHeader,"Product Id");

        workSheet.setColumnWidth(PRODUCT_NAME_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(PRODUCT_NAME_COL,rowHeader,"Product Name");

        workSheet.setColumnWidth(CURRENCY_COL,TemplatePopulateImportConstants.MEDIUM_COL_SIZE);
        writeString(CURRENCY_COL,rowHeader,"Currency");

        workSheet.setColumnWidth(DECIMAL_PLACES_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(DECIMAL_PLACES_COL,rowHeader,"Decimal Places");

        workSheet.setColumnWidth(TODAYS_PRICE_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(TODAYS_PRICE_COL,rowHeader,"Today's Price");

        workSheet.setColumnWidth(CURRENCY_IN_MULTIPLES_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(CURRENCY_IN_MULTIPLES_COL,rowHeader,"Currency in multiples of");

        workSheet.setColumnWidth(CHARGES_NAME_1_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(CHARGES_NAME_1_COL,rowHeader,"Charges Name 1");

        workSheet.setColumnWidth(CHARGES_ID_1_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(CHARGES_ID_1_COL,rowHeader,"Charges Id 1");

        workSheet.setColumnWidth(CHARGES_NAME_2_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(CHARGES_NAME_2_COL,rowHeader,"Charges Name 2");

        workSheet.setColumnWidth(CHARGES_ID_2_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(CHARGES_ID_2_COL,rowHeader,"Charges Id 2");

        workSheet.setColumnWidth(CHARGES_NAME_3_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(CHARGES_NAME_3_COL,rowHeader,"Charges Name 3");

        workSheet.setColumnWidth(CHARGES_ID_3_COL,TemplatePopulateImportConstants.SMALL_COL_SIZE);
        writeString(CHARGES_ID_3_COL,rowHeader,"Charges Id 3");

    }

    public List<ShareProductData> getSharedProductDataList() {
        return sharedProductDataList;
    }

    public Map<Long, Integer[]> getProductToBeginEndIndexesofCharges() {
        return productToBeginEndIndexesofCharges;
    }
}
