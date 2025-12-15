package ng.com.createsoftware.fn_payroll_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_payroll_service.model.PayrollRun;
import ng.com.createsoftware.fn_payroll_service.service.PayrollService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payroll")
@RequiredArgsConstructor
public class PayrollController {
    private final PayrollService payrollService;

    @PostMapping("/run")
    public ResponseEntity<PayrollRun> run(){
        return new ResponseEntity<>(payrollService.runPayroll(), HttpStatus.CREATED);
    }
}
