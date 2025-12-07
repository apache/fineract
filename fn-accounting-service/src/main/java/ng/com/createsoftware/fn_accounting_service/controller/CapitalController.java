package ng.com.createsoftware.fn_accounting_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.dto.request.CapitalRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.CapitalResponse;
import ng.com.createsoftware.fn_accounting_service.model.Capital;
import ng.com.createsoftware.fn_accounting_service.service.CapitalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/capitals")
@RequiredArgsConstructor
public class CapitalController {
    private final CapitalService capitalService;

    @GetMapping
    public ResponseEntity<List<CapitalResponse>> getCapitalsHandler(){
        return new ResponseEntity<>(capitalService.getCapitals(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CapitalResponse> addCapitalHandler(@RequestBody CapitalRequest request){
        return new ResponseEntity<>(capitalService.addCapital(request), HttpStatus.OK);
    }
}
