package ng.com.createsoftware.fn_accounting_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_accounting_service.service.BatchUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/batch")
@RequiredArgsConstructor
public class BatchUploadController {

    private final BatchUploadService batchUploadService;

    @PostMapping(value="/account-categories", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadCategoriesHandler(@RequestPart MultipartFile file) throws Exception{
        return new ResponseEntity<>(batchUploadService.uploadAccountCategories(file), HttpStatus.OK);
    }

    @PostMapping(value="/general-ledgers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadGeneralLedgerHandler(@RequestPart MultipartFile file) throws Exception{
        return new ResponseEntity<>(batchUploadService.uploadGeneralLedgers(file), HttpStatus.OK);
    }
}
