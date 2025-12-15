package ng.com.createsoftware.fn_payroll_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_payroll_service.model.Expense;
import ng.com.createsoftware.fn_payroll_service.repository.ExpenseRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseService {
    private final ExpenseRepository expenseRepository;

    public Expense create(Expense expense){
        return expenseRepository.save(expense);
    }

    public List<Expense> list(){
        return expenseRepository.findAll();
    }
}
