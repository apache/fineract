package org.mifosplatform.portfolio.account.data;

import org.mifosplatform.portfolio.group.service.SearchParameters;

public class StandingInstructionDTO {

    final SearchParameters searchParameters;
    final Long clientId;
    final String clientName;
    final Integer transferType;
    final Integer fromAccountType;
    final Long fromAccount;

    public StandingInstructionDTO(final SearchParameters searchParameters, final Integer transferType, final String clientName,
            final Long clientId, final Long fromAccount,final Integer fromAccountType) {
        this.searchParameters = searchParameters;
        this.transferType = transferType;
        this.clientName = clientName;
        this.clientId = clientId;
        this.fromAccount = fromAccount;
        this.fromAccountType = fromAccountType;
    }

    public SearchParameters searchParameters() {
        return this.searchParameters;
    }

    public Long clientId() {
        return this.clientId;
    }

    public String clientName() {
        return this.clientName;
    }

    public Integer transferType() {
        return this.transferType;
    }

    public Long fromAccount() {
        return this.fromAccount;
    }

    
    public Integer fromAccountType() {
        return this.fromAccountType;
    }

}
