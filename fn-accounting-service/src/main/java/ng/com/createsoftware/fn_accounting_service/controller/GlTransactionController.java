package ng.com.createsoftware.fn_accounting_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.dto.request.GLTransactionRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.GLTransactionResponse;
import ng.com.createsoftware.fn_accounting_service.model.GLTransaction;
import ng.com.createsoftware.fn_accounting_service.service.GLTransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/gl-transactions")
@RequiredArgsConstructor
public class GlTransactionController {
    private final GLTransactionService glTransactionService;

    @PostMapping
    public ResponseEntity<GLTransactionResponse> addGLTransactionsHandler(@RequestBody GLTransactionRequest request){
        return new ResponseEntity<>(glTransactionService.addGLTransaction(request), HttpStatus.CREATED);
    }

    @GetMapping("/ledger/{ledgerId}")
    public ResponseEntity<List<GLTransactionResponse>> getGLTransactions(@PathVariable Long ledgerId){
        return new ResponseEntity<>(glTransactionService.getGLTransaction(ledgerId), HttpStatus.OK);
    }
}
