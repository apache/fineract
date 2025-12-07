package ng.com.createsoftware.fn_comm_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_comm_service.dto.response.AuditPayload;
import ng.com.createsoftware.fn_comm_service.service.CommService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditListener {
    private final ObjectMapper mapper = new ObjectMapper();
    private final CommService commService;

    @RabbitListener(queues = "audit.accounting.queue")
    public void onAuditMessage(String payloadJson){
             try {
                 AuditPayload payload = mapper.readValue(payloadJson, AuditPayload.class);
                 //Decide routing: simple heuristic
                 String details = payload.getDetails() == null ? "" : payload.getDetails();
                 if(payload.getEventType() != null && payload.getEventType().contains("TRANSACTION")){
                     //send mail to local bank
//                     commService.sendMessage();
                     log.info("Send email");
                 }else{
                     log.info("Send sms");
                 }
                  }catch(Exception ex){
                     System.out.println(ex.getMessage());
                  }
    }
}
