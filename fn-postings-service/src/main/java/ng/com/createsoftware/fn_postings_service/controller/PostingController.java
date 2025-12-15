package ng.com.createsoftware.fn_postings_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_postings_service.dto.request.AccountTransferRequest;
import ng.com.createsoftware.fn_postings_service.dto.request.DepositRequest;
import ng.com.createsoftware.fn_postings_service.dto.request.ReversalRequest;
import ng.com.createsoftware.fn_postings_service.dto.request.WithdrawalRequest;
import ng.com.createsoftware.fn_postings_service.model.PostingTransaction;
import ng.com.createsoftware.fn_postings_service.service.PostingService;
import ng.com.createsoftware.fn_postings_service.service.ReversalService;
import ng.com.createsoftware.fn_postings_service.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posting")
public class PostingController {
    private final PostingService postingService;
    private final TransferService transferService;
    private final ReversalService reversalService;

    @PostMapping("/deposit")
    public ResponseEntity<PostingTransaction> depositHandler (@RequestBody DepositRequest request){
        return new ResponseEntity<>(postingService.deposit(request), HttpStatus.OK);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<PostingTransaction> withdrawHandler (@RequestBody WithdrawalRequest request){
        return new ResponseEntity<>(postingService.withdraw(request), HttpStatus.OK);
    }

    @PostMapping("/transfer")
    public ResponseEntity<PostingTransaction> transferHandler (@RequestBody AccountTransferRequest request){
        return new ResponseEntity<>(transferService.transfer(request), HttpStatus.OK);
    }

    @PostMapping("/reverse")
    public ResponseEntity<String> reverseHandler (@RequestBody ReversalRequest request){
        reversalService.reverse(request);
        return new ResponseEntity<>("Successfully Reversed.", HttpStatus.OK);
    }
}
