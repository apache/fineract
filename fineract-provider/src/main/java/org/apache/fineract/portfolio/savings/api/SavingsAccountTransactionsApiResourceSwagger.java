package org.apache.fineract.portfolio.savings.api;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.math.BigDecimal;

/**
 * @author biandra
 */
public class SavingsAccountTransactionsApiResourceSwagger {
    private SavingsAccountTransactionsApiResourceSwagger() {
    }

    @ApiModel(value = "PostTransactionRequest")
    public final static class PostTransactionRequest {
        private PostTransactionRequest() {
        }

        @ApiModelProperty(example = "1")
        public Long paymentTypeId;
        @ApiModelProperty(example = "1")
        public BigDecimal transactionAmount;
        @ApiModelProperty(example = "dd MMMM yyyy")
        public String dateFormat;
        @ApiModelProperty(example = "en")
        public String locale;
        @ApiModelProperty(example = "04 March 2009")
        public String transactionDate;
    }

    @ApiModel(value = "PostTransactionResponse")
    public final static class PostTransactionResponse {
        private PostTransactionResponse() {
        }

        @ApiModelProperty(example = "1")
        public Integer officeId;
        @ApiModelProperty(example = "2")
        public Integer clientId;
        @ApiModelProperty(example = "1")
        public Integer savingsId;
        @ApiModelProperty(example = "2")
        public Integer resourceId;
    }

}
