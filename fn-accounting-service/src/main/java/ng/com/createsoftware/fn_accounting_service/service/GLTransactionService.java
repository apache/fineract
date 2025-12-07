package ng.com.createsoftware.fn_accounting_service.service;

import ng.com.createsoftware.fn_accounting_service.dto.request.GLTransactionRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.GLTransactionResponse;

import java.util.List;

public interface GLTransactionService {

    GLTransactionResponse addGLTransaction(GLTransactionRequest request);
    List<GLTransactionResponse> getGLTransaction(Long ledgerId);
}
