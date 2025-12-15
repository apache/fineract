package ng.com.createsoftware.fn_agency_banking_service.service;

import ng.com.createsoftware.fn_agency_banking_service.dto.request.TillCustomerRequest;
import ng.com.createsoftware.fn_agency_banking_service.dto.request.TillVaultRequest;
import ng.com.createsoftware.fn_agency_banking_service.dto.response.TillTransactionResponse;
import ng.com.createsoftware.fn_agency_banking_service.model.TillTransaction;

public interface TillVaultService {
    TillTransactionResponse vaultToTill(TillVaultRequest request);
    TillTransactionResponse tillToVault(TillVaultRequest request);
    TillTransactionResponse tillToCustomer(TillCustomerRequest request);
    TillTransactionResponse customerToTill(TillCustomerRequest request);
}
