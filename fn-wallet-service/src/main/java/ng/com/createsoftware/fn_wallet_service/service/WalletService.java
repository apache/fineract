package ng.com.createsoftware.fn_wallet_service.service;

import ng.com.createsoftware.fn_wallet_service.dto.request.WalletTopupRequest;

public interface WalletService {
    void topupWallet(WalletTopupRequest request);
}
