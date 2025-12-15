package ng.com.createsoftware.fn_customer_service.mapper;

import ng.com.createsoftware.fn_customer_service.dto.response.CustomerResponse;
import ng.com.createsoftware.fn_customer_service.model.Customer;
import ng.com.createsoftware.fn_customer_service.model.Status;

public class CustomerMapper {
    public static CustomerResponse customerToCustomerResponse(Customer customer){
        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());
        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setPhone(customer.getPhone());
        response.setEmail(customer.getEmail());
        response.setBvn(customer.getBvn());
        response.setStatus(customer.getStatus().name());
        return  response;
    }

    public static Customer customerResponseToCustomer(CustomerResponse response){
        Customer customer = new Customer();
        customer.setId(response.getId());
        customer.setFirstName(response.getFirstName());
        customer.setLastName(response.getLastName());
        customer.setPhone(response.getPhone());
        customer.setEmail(response.getEmail());
        customer.setBvn(response.getBvn());
        customer.setStatus(Status.valueOf(response.getStatus()));
        return customer;
    }
}
