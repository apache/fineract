package ng.com.createsoftware.fn_wallet_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_wallet_service.dto.request.WalletTopupRequest;
import ng.com.createsoftware.fn_wallet_service.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/topup")
    public ResponseEntity<String> topupWalletHandler(@RequestBody WalletTopupRequest request){
        walletService.topupWallet(request);
        return new ResponseEntity<>("Topup of wallet initiated", HttpStatus.OK);
    }
}
