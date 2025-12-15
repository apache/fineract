package ng.com.createsoftware.fn_customer_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_customer_service.dto.request.AddCustomerAccountRequest;
import ng.com.createsoftware.fn_customer_service.dto.request.AddCustomerRequest;
import ng.com.createsoftware.fn_customer_service.dto.response.CustomerAccountResponse;
import ng.com.createsoftware.fn_customer_service.dto.response.CustomerResponse;
import ng.com.createsoftware.fn_customer_service.mapper.CustomerAccountMapper;
import ng.com.createsoftware.fn_customer_service.mapper.CustomerMapper;
import ng.com.createsoftware.fn_customer_service.model.BvnService;
import ng.com.createsoftware.fn_customer_service.model.Customer;
import ng.com.createsoftware.fn_customer_service.model.CustomerAccount;
import ng.com.createsoftware.fn_customer_service.model.Status;
import ng.com.createsoftware.fn_customer_service.repository.CustomerAccountRepository;
import ng.com.createsoftware.fn_customer_service.repository.CustomerReposiitory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService{
    private final CustomerReposiitory customerReposiitory;
    private final CustomerAccountRepository customerAccountRepository;
    private final BvnService bvnService;
    private final CustomerEventPublisher publisher;

    @Transactional
    @Override
    public CustomerResponse addCustomer(AddCustomerRequest request) {
        var customer = new Customer();
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setBvn(request.getBvn());
        customer = customerReposiitory.save(customer);
        publisher.publishCreated(customer.getId(), customer.getFirstName(), customer.getLastName());
        return CustomerMapper.customerToCustomerResponse(customer);
    }

    @Override
    public List<CustomerResponse> listCustomersByStatus(Status status) {
        return customerReposiitory.findByStatus(status).stream()
                .map(CustomerMapper::customerToCustomerResponse)
                .toList();
    }

    @Override
    public List<CustomerResponse> getCustomers() {
        return customerReposiitory.findAll().stream()
                .map(CustomerMapper::customerToCustomerResponse)
                .toList();
    }

    @Override
    public CustomerResponse getCustomer(Long customerId) {
        return CustomerMapper.customerToCustomerResponse(customerReposiitory.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found")));
    }

    @Transactional
    @Override
    public CustomerResponse changeCustomerStatus(Long customerId, Status status) {
        CustomerResponse customerResponse = getCustomer(customerId);
        Customer customer = CustomerMapper.customerResponseToCustomer(customerResponse);
        customer.setStatus(status);
        customer  = customerReposiitory.save(customer);
        return CustomerMapper.customerToCustomerResponse(customer);
    }

    @Override
    public CustomerResponse verifyBvn(Long customerId) {
       var customerResponse = getCustomer(customerId);
       var customer = CustomerMapper.customerResponseToCustomer(customerResponse);
       var result = bvnService.verify(customer.getBvn(), customer.getFirstName(), customer.getLastName());
       if(result.verified()) customer.setStatus(Status.ACTIVE);
       customerReposiitory.save(customer);
       publisher.publishBvnVerified(customer.getId(), customer.getBvn(), result.verified());
       return CustomerMapper.customerToCustomerResponse(customer);
    }

    @Transactional
    @Override
    public CustomerAccountResponse addAccount(Long customerId, AddCustomerAccountRequest request) {
        CustomerResponse customerResponse = getCustomer(customerId);
        Customer customer = CustomerMapper.customerResponseToCustomer(customerResponse);
        var acct = new CustomerAccount(request.getAccountNumber(), request.getProductCode(), customer);
        acct.setCurrency(request.getCurrency());
        acct = customerAccountRepository.save(acct);
        return CustomerAccountMapper.customerAccountToCustomerAccountResponse(acct);
    }

    @Override
    public List<CustomerAccountResponse> listAccounts(Long customerId) {
        return customerAccountRepository.findByCustomerId(customerId).stream()
                .map(CustomerAccountMapper::customerAccountToCustomerAccountResponse)
                .toList();
    }
}
