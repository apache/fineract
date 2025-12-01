package ng.com.createsoftware.fn_comm_service.dto.request;

import lombok.Data;

@Data
public class MessageRequest {
    private String destination;
    private String subject;
    private String content;
    private String type;
}
