package ng.com.createsoftware.fn_investment_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_investment_service.dto.request.AddFixedDepositRequest;
import ng.com.createsoftware.fn_investment_service.model.FixedDepositAccount;
import ng.com.createsoftware.fn_investment_service.service.FixedDepositService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fd")
@RequiredArgsConstructor
public class FixedDepositController {
    private final FixedDepositService service;

    @PostMapping
    public ResponseEntity<FixedDepositAccount> createHandler(@RequestBody AddFixedDepositRequest request){
        return new ResponseEntity<>(service.created(request), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/liquidate")
    public ResponseEntity<FixedDepositAccount> liquidateHandler(@RequestBody AddFixedDepositRequest request){
        return new ResponseEntity<>(service.created(request), HttpStatus.CREATED);
    }
}
