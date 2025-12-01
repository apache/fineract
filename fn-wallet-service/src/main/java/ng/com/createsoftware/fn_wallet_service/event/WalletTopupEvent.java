package ng.com.createsoftware.fn_wallet_service.event;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_wallet_service.dto.request.WalletTopupRequest;
import org.springframework.context.ApplicationEvent;


public class WalletTopupEvent extends ApplicationEvent {
    private final WalletTopupRequest request;

    public WalletTopupEvent(Object source, WalletTopupRequest request) {
        super(source);
        this.request = request;
    }

    public WalletTopupRequest getRequest(){
        return request;
    }
}
