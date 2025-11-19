package org.apache.fineract.portfolio.loanaccount.mapper;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.apache.fineract.portfolio.collateralmanagement.domain.ClientCollateralManagement;
import org.apache.fineract.portfolio.loanaccount.data.LoanCollateralManagementData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCollateralManagement;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-19T12:31:06-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.7 (Oracle Corporation)"
)
@Component
public class LoanCollateralManagementMapperImpl implements LoanCollateralManagementMapper {

    @Override
    public LoanCollateralManagementData map(LoanCollateralManagement loanCollateralManagement) {
        if ( loanCollateralManagement == null ) {
            return null;
        }

        Long id = null;
        Long clientCollateralId = null;
        BigDecimal quantity = null;

        id = loanCollateralManagement.getId();
        clientCollateralId = loanCollateralManagementClientCollateralManagementId( loanCollateralManagement );
        quantity = loanCollateralManagement.getQuantity();

        BigDecimal total = null;
        BigDecimal totalCollateral = null;

        LoanCollateralManagementData loanCollateralManagementData = new LoanCollateralManagementData( clientCollateralId, quantity, total, totalCollateral, id );

        return loanCollateralManagementData;
    }

    @Override
    public Set<LoanCollateralManagementData> map(Set<LoanCollateralManagement> loanCollateralManagements) {
        if ( loanCollateralManagements == null ) {
            return null;
        }

        Set<LoanCollateralManagementData> set = LinkedHashSet.newLinkedHashSet( loanCollateralManagements.size() );
        for ( LoanCollateralManagement loanCollateralManagement : loanCollateralManagements ) {
            set.add( map( loanCollateralManagement ) );
        }

        return set;
    }

    private Long loanCollateralManagementClientCollateralManagementId(LoanCollateralManagement loanCollateralManagement) {
        ClientCollateralManagement clientCollateralManagement = loanCollateralManagement.getClientCollateralManagement();
        if ( clientCollateralManagement == null ) {
            return null;
        }
        return clientCollateralManagement.getId();
    }
}
