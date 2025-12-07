package ng.com.createsoftware.fn_accounting_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.batch.BatchParser;
import ng.com.createsoftware.fn_accounting_service.dto.request.GLTransactionRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.GLTransactionResponse;
import ng.com.createsoftware.fn_accounting_service.events.AccountingEventPublisher;
import ng.com.createsoftware.fn_accounting_service.mapper.GLTransactionMapper;
import ng.com.createsoftware.fn_accounting_service.model.AccountCategory;
import ng.com.createsoftware.fn_accounting_service.model.GLTransaction;
import ng.com.createsoftware.fn_accounting_service.model.GeneralLedger;
import ng.com.createsoftware.fn_accounting_service.repository.AccountCategoryRepository;
import ng.com.createsoftware.fn_accounting_service.repository.GeneralLedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchUploadServiceImpl  implements BatchUploadService{
    private final AccountCategoryRepository accountCategoryRepository;
    private final GeneralLedgerRepository generalLedgerRepository;
    private final GLTransactionService glTransactionService;
    private final AccountingEventPublisher accountingEventPublisher;

    @Transactional
    @Override
    public Map<String, Object> uploadAccountCategories(MultipartFile file) throws IOException {
        List<Map<String, String>> rows = BatchParser.parse(file);
        int created = 0;
        List<String>errors = new ArrayList<>();
        for(int i = 0; i < rows.size(); i++ ){
            Map<String, String> row = rows.get(i);
                 try {
                     String name = row.getOrDefault("name", row.getOrDefault("Name", "")).trim();
                     String desc = row.getOrDefault("description", row.getOrDefault("Description", "")).trim();
                     if(name.isEmpty()) throw new IllegalArgumentException("Name empty at row " + (i + 1));
                     AccountCategory category = AccountCategory.builder()
                             .name(name)
                             .description(desc)
                             .build();
                     accountCategoryRepository.save(category);
                     created++;
                      }catch(Exception ex){
                         System.out.println(ex.getMessage());
                         errors.add("row " + (i + 1) + ": " + ex.getMessage());
                      }
        }
        return Map.of("created", created, "errors", errors);
    }

    @Override
    public Map<String, Object> uploadGeneralLedgers(MultipartFile file) throws IOException {
        List<Map<String,String>> rows = BatchParser.parse(file);
        int created = 0;
        List<String> errors = new ArrayList<>();
        for(int i = 0; i < rows.size(); i++ ){
            Map<String, String> row = rows.get(i);
                 try {
                         String code = row.getOrDefault("code", row.getOrDefault("Code", "")).trim();
                         String name = row.getOrDefault("name", row.getOrDefault("Name", "")).trim();
                         if(code.isEmpty() || name.isEmpty()) throw new IllegalArgumentException("Code/name empty at row " + (i + 1));
                         GeneralLedger ledger = GeneralLedger.builder()
                                 .code(code)
                                 .name(name)
                                 .balance(BigDecimal.ZERO)
                                 .build();
                         generalLedgerRepository.save(ledger);
                         created++;
                      }catch(Exception ex){
                         System.out.println(ex.getMessage());
                     errors.add("row " + (i + 1) + ": " + ex.getMessage());
                      }
              }
        return Map.of("created", created, "errors", errors);
    }

    @Override
    public List<GLTransaction> processBatch(List<GLTransactionRequest> transactions) {
         List<GLTransaction> results = new ArrayList<>();

         for(GLTransactionRequest request: transactions){
             GLTransactionResponse savedTransaction = glTransactionService.addGLTransaction(request);

             results.add(GLTransactionMapper.gLTransactionResponseToGLTransaction(savedTransaction));
         }
         accountingEventPublisher.publishBatchEvent(results.size());
         return results;
    }
}
