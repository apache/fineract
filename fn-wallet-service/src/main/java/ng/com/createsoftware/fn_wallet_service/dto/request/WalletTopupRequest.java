package ng.com.createsoftware.fn_wallet_service.dto.request;

import lombok.Data;

@Data
public class WalletTopupRequest {
    private Long clientId;
    private Double amount;
    private String channel;
    private String phone;
}
