package ng.com.createsoftware.fn_accounting_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ng.com.createsoftware.fn_accounting_service.dto.request.CapitalRequest;
import ng.com.createsoftware.fn_accounting_service.dto.response.CapitalResponse;
import ng.com.createsoftware.fn_accounting_service.model.Capital;
import ng.com.createsoftware.fn_accounting_service.repository.CapitalRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapitalServiceImpl implements CapitalService{

    private final CapitalRepository capitalRepository;

    @Override
    public List<CapitalResponse> getCapitals() {
        return capitalRepository.findAll().stream()
                .map(c -> CapitalResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .amount(c.getAmount()).build()
                ).toList();
    }

    @Transactional
    @Override
    public CapitalResponse addCapital(CapitalRequest request) {
        Capital capital = Capital.builder()
                .name(request.getName())
                .amount(request.getAmount())
                .build();
        capital = capitalRepository.save(capital);
        return CapitalResponse.builder().id(capital.getId()).name(capital.getName()).amount(capital.getAmount()).build();
    }
}
