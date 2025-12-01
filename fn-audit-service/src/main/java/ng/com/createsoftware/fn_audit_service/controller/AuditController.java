package ng.com.createsoftware.fn_audit_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_audit_service.dto.request.AuditRequest;
import ng.com.createsoftware.fn_audit_service.model.AuditLog;
import ng.com.createsoftware.fn_audit_service.service.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/audits")
@RequiredArgsConstructor
public class AuditController {
    private final AuditService auditService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> allAuditsHandler(){
        return new ResponseEntity<>(auditService.allAudits(), HttpStatus.OK);
    }
    //htis is to try it
    @PostMapping("/log")
    public ResponseEntity<String> auditLogHandler(@RequestBody AuditRequest request){
        auditService.auditLog(request);
        return new ResponseEntity<>("Audit Logs", HttpStatus.OK);
    }
}
