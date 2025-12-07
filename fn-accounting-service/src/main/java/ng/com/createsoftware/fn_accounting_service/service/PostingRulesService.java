package ng.com.createsoftware.fn_accounting_service.service;

import java.math.BigDecimal;

public interface PostingRulesService {
    void validatePosting(long glId, BigDecimal signedChange);
    void setPreventNegativeBalances(boolean prevent);
    boolean isPreventNegativeBalance();
}
