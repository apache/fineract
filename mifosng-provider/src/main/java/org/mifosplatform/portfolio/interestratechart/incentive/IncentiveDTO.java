package org.mifosplatform.portfolio.interestratechart.incentive;

import java.math.BigDecimal;

import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.interestratechart.domain.InterestIncentivesFields;

public class IncentiveDTO {

    private final Client client;
    private final BigDecimal interest;
    private final InterestIncentivesFields incentives;

    public IncentiveDTO(final Client client, final BigDecimal interest, final InterestIncentivesFields incentives) {
        this.client = client;
        this.interest = interest;
        this.incentives = incentives;
    }

    public Client client() {
        return this.client;
    }

    public BigDecimal interest() {
        return this.interest;
    }

    public InterestIncentivesFields incentives() {
        return this.incentives;
    }
}
