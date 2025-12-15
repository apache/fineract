package ng.com.createsoftware.fn_customer_service.model;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
@Slf4j
public class BvnService {
    public record BvnResult(boolean verified, String bvn, String message){}
    public BvnResult verify(String bvn, String firstName, String lastName){
        if(bvn == null || bvn.trim().length() != 11)
            return  new BvnResult(false, bvn, "Invalid BVN format");
        boolean verified = new Random(bvn.hashCode()).nextInt(100) > 10;
        return new BvnResult(verified, bvn, verified ? "OK" : "NOT_MATCH");
    }

}
