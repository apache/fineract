package ng.com.createsoftware.fn_wallet_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_wallet_service.dto.request.WalletTopupRequest;
import ng.com.createsoftware.fn_wallet_service.event.WalletTopupEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletServiceImpl implements  WalletService{

    private final ApplicationEventPublisher applicationEventPublisher;
    private final AuditPublisherService auditPublisherService;
    @Override
    public void topupWallet(WalletTopupRequest request) {
        log.info("Top up Wallet");
        applicationEventPublisher.publishEvent(new WalletTopupEvent(this, request));
        auditPublisherService.publishWalletTopup(request.getClientId(), request.getAmount(), request.getPhone());
    }
}
