package ng.com.createsoftware.fn_agency_service.controller;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_agency_service.dto.request.AgencyRequest;
import ng.com.createsoftware.fn_agency_service.service.AgencyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agencies")
@RequiredArgsConstructor
public class AgencyController {
    private final AgencyService agencyService;

    @PostMapping("/process")
    public ResponseEntity<String> processHandler(@RequestBody AgencyRequest request){
        return new ResponseEntity<>(agencyService.process(request), HttpStatus.CREATED);
    }
}
