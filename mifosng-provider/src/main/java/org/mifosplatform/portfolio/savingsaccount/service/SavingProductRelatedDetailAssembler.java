package org.mifosplatform.portfolio.savingsaccount.service;

import org.mifosplatform.portfolio.savingsaccount.command.CalculateSavingScheduleCommand;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProduct;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRelatedDetail;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductRepository;
import org.mifosplatform.portfolio.savingsaccountproduct.exception.SavingProductNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SavingProductRelatedDetailAssembler {

    private final SavingProductRepository savingProductRepository;

    @Autowired
    public SavingProductRelatedDetailAssembler(final SavingProductRepository savingProductRepository) {
        this.savingProductRepository = savingProductRepository;
    }

    public SavingProductRelatedDetail assembleFrom(final CalculateSavingScheduleCommand command) {

//        final BigDecimal depositAmountForPeriod = command.getDeposit();
//        final BigDecimal depositInterestRate = command.getInterestRate();
//        final LocalDate installmentDate = command.getPaymentsStartingFromDate();
//        final Integer payEvery = command.getPayEvery();
//        final Integer paymentFrequencyType = command.getPaymentFrequencyType();

        final SavingProduct savingProduct = this.savingProductRepository.findOne(command.getProductId());
        if (savingProduct == null) { throw new SavingProductNotFoundException(command.getProductId()); }

//        final MonetaryCurrency currency = savingProduct.getCurrency();

        return null;
    }

}
