package ng.com.createsoftware.fn_agency_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_agency_service.dto.request.AgencyRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgencyServiceImpl implements AgencyService{
    @Override
    public String process(AgencyRequest request) {
         var ch = request.getChannel() == null ? "" : request.getChannel().toUpperCase();
         return switch (ch){
             case "USSD" -> processUSSD(request);
             case "POS" -> processPOS(request);
             case "ATM" -> processATM(request);
             default -> "UNKNOWN CHANNEL";
         };
    }

    private String processUSSD(AgencyRequest request){
        return "USSD transaction processed";
    }
    private String processPOS(AgencyRequest request){
        return "POS transaction processed";
    }
    private String processATM(AgencyRequest request){
        return "ATM transaction processed";
    }
}
