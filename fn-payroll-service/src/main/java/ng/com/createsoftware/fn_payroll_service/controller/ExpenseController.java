package ng.com.createsoftware.fn_payroll_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_payroll_service.model.Expense;
import ng.com.createsoftware.fn_payroll_service.model.PayrollRun;
import ng.com.createsoftware.fn_payroll_service.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<Expense> createHandler(@RequestBody Expense expense){
        return new ResponseEntity<>(expenseService.create(expense), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Expense>> list(){
        return new ResponseEntity<>(expenseService.list(), HttpStatus.OK);
    }
}
