package ng.com.createsoftware.fn_wallet_service.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletTopupRequest {
    private Long clientId;
    private BigDecimal amount;
    private String channel;
    private String phone;
}
