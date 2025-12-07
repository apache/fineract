package ng.com.createsoftware.fn_accounting_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.request.GLTransactionRequest;
import ng.com.createsoftware.fn_accounting_service.model.GLTransaction;
import ng.com.createsoftware.fn_accounting_service.repository.AccountCategoryRepository;
import ng.com.createsoftware.fn_accounting_service.repository.GeneralLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface BatchUploadService {
  Map<String,Object> uploadAccountCategories(MultipartFile file) throws IOException;
  Map<String, Object> uploadGeneralLedgers(MultipartFile file) throws IOException;
  List<GLTransaction> processBatch(List<GLTransactionRequest> transactions);

}
