package ng.com.createsoftware.fn_comm_service.service;

import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_comm_service.dto.request.MessageRequest;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CommServiceImpl implements CommService {
    @Override
    public void sendMessage(MessageRequest request) {
        if("EMAIL".equalsIgnoreCase(request.getType()))
            sendEmail(request);
        else sendSms(request);
    }


    private void sendEmail(MessageRequest request) {
        log.info("SEND EMAIL to {}, subject: {}, body: {}", request.getDestination(), request.getSubject(), request.getContent());
    }


    private void sendSms(MessageRequest request) {
        log.info("SEND SMS to {}, subject: {}, body: {}", request.getDestination(), request.getSubject(), request.getContent());
    }
}
