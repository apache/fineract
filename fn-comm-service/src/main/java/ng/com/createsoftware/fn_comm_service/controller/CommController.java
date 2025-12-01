package ng.com.createsoftware.fn_comm_service;

import lombok.RequiredArgsConstructor;
import ng.com.createsoftware.fn_comm_service.dto.request.MessageRequest;
import ng.com.createsoftware.fn_comm_service.service.CommService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/comms")
@RequiredArgsConstructor
public class CommController {
    private final CommService commService;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessageHandler(@RequestBody MessageRequest request){
        commService.sendMessage(request);
        return new ResponseEntity<>("Message queued and sent", HttpStatus.OK);
    }
}
