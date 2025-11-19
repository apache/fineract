package org.apache.fineract.portfolio.loanaccount.mapper;

import javax.annotation.processing.Generated;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.command.LoanChargeCommand;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:07-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class LoanChargeMapperImpl implements LoanChargeMapper {

    @Override
    public LoanChargeCommand map(LoanCharge loanCharge, MonetaryCurrency currency) {
        if ( loanCharge == null && currency == null ) {
            return null;
        }

        LoanChargeCommand loanChargeCommand = new LoanChargeCommand();

        if ( loanCharge != null ) {
            loanChargeCommand.setId( loanCharge.getId() );
            loanChargeCommand.setChargeId( loanChargeChargeId( loanCharge ) );
            loanChargeCommand.setChargeTimeType( loanChargeChargeTimeTypeValue( loanCharge ) );
            loanChargeCommand.setChargeCalculationType( loanChargeChargeCalculationValue( loanCharge ) );
            loanChargeCommand.setDueDate( loanCharge.getDueDate() );
        }
        loanChargeCommand.setAmount( loanCharge.getAmount(currency).getAmount() );

        return loanChargeCommand;
    }

    private Long loanChargeChargeId(LoanCharge loanCharge) {
        Charge charge = loanCharge.getCharge();
        if ( charge == null ) {
            return null;
        }
        return charge.getId();
    }

    private Integer loanChargeChargeTimeTypeValue(LoanCharge loanCharge) {
        ChargeTimeType chargeTimeType = loanCharge.getChargeTimeType();
        if ( chargeTimeType == null ) {
            return null;
        }
        return chargeTimeType.getValue();
    }

    private Integer loanChargeChargeCalculationValue(LoanCharge loanCharge) {
        ChargeCalculationType chargeCalculation = loanCharge.getChargeCalculation();
        if ( chargeCalculation == null ) {
            return null;
        }
        return chargeCalculation.getValue();
    }
}
