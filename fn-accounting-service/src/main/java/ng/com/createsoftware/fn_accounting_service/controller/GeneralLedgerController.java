package ng.com.createsoftware.fn_accounting_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.dto.request.GeneralLedgerRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.GeneralLedgerResponse;
import ng.com.createsoftware.fn_accounting_service.service.GeneralLedgerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/gl")
@RequiredArgsConstructor
public class GeneralLedgerController {
    private final GeneralLedgerService generalLedgerService;

    @GetMapping
    public ResponseEntity<List<GeneralLedgerResponse>> getGeneralLedgersHandler(){
        return new ResponseEntity<>(generalLedgerService.getGeneralLedgers(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<GeneralLedgerResponse> addGeneralLedgerHandler(@RequestBody GeneralLedgerRequest request){
        return new ResponseEntity<>(generalLedgerService.addGeneralLedger(request), HttpStatus.CREATED);
    }

    @PostMapping("/{ledgerId}/fund")
    public ResponseEntity<GeneralLedgerResponse> addGeneralLedgerFundHandler(@PathVariable Long ledgerId, @RequestParam BigDecimal amount, @RequestParam(required=false) String narration){
        return new ResponseEntity<>(generalLedgerService.generalLedgerFund(ledgerId, amount, narration), HttpStatus.OK);
    }
}
