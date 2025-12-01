package ng.com.createsoftware.fn_wallet_service.event;

import ng.com.createsoftware.fn_wallet_service.dto.request.WalletTopupRequest;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class WalletTopupHttpNotifier {
    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener
    public void onTopup(WalletTopupEvent event) {
        var rqt = event.getRequest();
        try {
            /// add listener
        }catch(Exception ex){
            System.out.println(ex.getMessage());;
        }
    }
}
