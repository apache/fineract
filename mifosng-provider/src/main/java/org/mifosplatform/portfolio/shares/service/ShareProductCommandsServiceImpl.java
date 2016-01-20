/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.shares.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.portfolio.accounts.domain.ShareAccount;
import org.mifosplatform.portfolio.products.service.ProductCommandsService;
import org.mifosplatform.portfolio.shares.constants.ShareProductApiConstants;
import org.mifosplatform.portfolio.shares.data.DividendsData;
import org.mifosplatform.portfolio.shares.data.ProductDividendsData;
import org.mifosplatform.portfolio.shares.domain.ShareProduct;
import org.mifosplatform.portfolio.shares.domain.ShareProductTempRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;

@Service(value = "SHAREPRODUCT_COMMANDSERVICE")
public class ShareProductCommandsServiceImpl implements ProductCommandsService {

    private final FromJsonHelper fromApiJsonHelper;

    @Autowired
    public ShareProductCommandsServiceImpl(final FromJsonHelper fromApiJsonHelper) {
        this.fromApiJsonHelper = fromApiJsonHelper;
    }

    public ProductDividendsData previewDividends(Long productId, JsonCommand jsonCommand) {
        ArrayList<ShareAccount> accounts = ShareProductTempRepository.getInstance().getAllAccounts(productId);
        ShareProduct product = ShareProductTempRepository.getInstance().fineOne(productId);
        Long total = product.getTotalShares();
        JsonElement element = jsonCommand.parsedJson();
        final BigDecimal totalDividendAmount = this.fromApiJsonHelper.extractBigDecimalWithLocaleNamed("dividendAmount", element);
        BigDecimal perShareValue = totalDividendAmount.divide(new BigDecimal(total));
        Date date = new Date();
        ArrayList<DividendsData> dividends = new ArrayList<>();
        for (ShareAccount account : accounts) {
            if(account.getStatus().equals("Approved")) {
                BigDecimal val = perShareValue.multiply(new BigDecimal(account.getTotalShares()));
                DividendsData data = new DividendsData(account.getClientId(), account.getClientName(), account.getSavingsAccountNo(),
                        account.getTotalShares(), val, date);
                dividends.add(data);    
            }
        }
        
        MonetaryCurrency currency = product.getCurrency() ;
        CurrencyData cur =  new CurrencyData(currency.getCode(), "", currency.getDigitsAfterDecimal(), currency.getCurrencyInMultiplesOf(),
                "", "");
        ProductDividendsData toReturn = new ProductDividendsData(productId, product.getProductName(), date, totalDividendAmount, cur, dividends);
        return toReturn;
    }

    public CommandProcessingResult postDividends(Long productId, JsonCommand jsonCommand) {
        try {
            ProductDividendsData data = previewDividends(productId, jsonCommand);
            ShareProductTempRepository.getInstance().saveDividends(data);
            return new CommandProcessingResultBuilder() //
                    .withCommandId(jsonCommand.commandId()) //
                    .withEntityId(data.getId()) //
                    .build();
        } catch (final DataIntegrityViolationException dve) {
            return CommandProcessingResult.empty();
        }
    }

    @Override
    public Object handleCommand(Long productId, String command, String jsonBody) {
        final JsonElement parsedCommand = this.fromApiJsonHelper.parse(jsonBody);
        final JsonCommand jsonCommand = JsonCommand.from(jsonBody, parsedCommand, this.fromApiJsonHelper, null, null, null, null, null,
                null, null, null, null, null);
        if (ShareProductApiConstants.PREIEW_DIVIDENDS_COMMAND_STRING.equals(command)) {
            return previewDividends(productId, jsonCommand);
        } else if (ShareProductApiConstants.POST_DIVIDENdS_COMMAND_STRING.equals(command)) { return postDividends(productId,
                jsonCommand); }
        // throw unknow commandexception
        return CommandProcessingResult.empty();
    }

}
