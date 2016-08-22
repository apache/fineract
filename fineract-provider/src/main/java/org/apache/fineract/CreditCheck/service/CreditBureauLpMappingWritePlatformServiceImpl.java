package org.apache.fineract.CreditCheck.service;

import org.apache.fineract.CreditCheck.domain.CreditBureauLpMapping;
import org.apache.fineract.CreditCheck.domain.CreditBureauLpMappingRepository;
import org.apache.fineract.CreditCheck.domain.OrganisationCreditBureau;
import org.apache.fineract.CreditCheck.domain.OrganisationCreditBureauRepository;
import org.apache.fineract.CreditCheck.serialization.CBLPCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreditBureauLpMappingWritePlatformServiceImpl implements CreditBureauLpMappingWritePlatformService
{

    
    private final PlatformSecurityContext context;
    
    private final CreditBureauLpMappingRepository  creditbureauLpMappingRepository;
    
    private final OrganisationCreditBureauRepository orgCbRepository;
    
    private final LoanProductRepository lpRepository;
    
    private final CBLPCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    
    @Autowired
    public CreditBureauLpMappingWritePlatformServiceImpl(final PlatformSecurityContext context,final CreditBureauLpMappingRepository  creditbureauLpMappingRepository,
            final OrganisationCreditBureauRepository orgCbRepository,LoanProductRepository lpRepository,final CBLPCommandFromApiJsonDeserializer fromApiJsonDeserializer )
    {
      this.context=context;
      this.creditbureauLpMappingRepository=creditbureauLpMappingRepository;
      this.orgCbRepository=orgCbRepository;
      this.lpRepository=lpRepository;
      this.fromApiJsonDeserializer=fromApiJsonDeserializer;
      
    }
    
    @Transactional
    @Override
    public CommandProcessingResult addCbLpMapping(Long cb_id,JsonCommand command) {
        this.context.authenticatedUser();
      this.fromApiJsonDeserializer.validateForCreate(command.json(),cb_id);
        
        
        
        final long lpid=command.longValueOfParameterNamed("loan_product_id");
        
        final OrganisationCreditBureau orgcb = this.orgCbRepository.getOne(cb_id);
        
        final LoanProduct lp=this.lpRepository.getOne(lpid);
        
        final CreditBureauLpMapping cb_lp=CreditBureauLpMapping.fromJson(command, orgcb, lp);
        
        this.creditbureauLpMappingRepository.save(cb_lp);
        
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(cb_lp.getId()).build();

    }

    @Override
    public CommandProcessingResult updateCreditBureauLoanProductMapping(JsonCommand command) {
       
        final Long mappingid=command.longValueOfParameterNamed("creditbureauLoanProductMappingId");
        final boolean is_active=command.booleanPrimitiveValueOfParameterNamed("is_active");
        final CreditBureauLpMapping cblpmapping=this.creditbureauLpMappingRepository.getOne(mappingid);
        cblpmapping.setIs_active(is_active);
        this.creditbureauLpMappingRepository.saveAndFlush(cblpmapping);
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(cblpmapping.getId()).build();
    }
}
