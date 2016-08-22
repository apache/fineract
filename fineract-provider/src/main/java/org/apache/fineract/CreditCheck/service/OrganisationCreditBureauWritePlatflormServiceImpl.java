package org.apache.fineract.CreditCheck.service;

import org.apache.fineract.CreditCheck.domain.CreditBureau;
import org.apache.fineract.CreditCheck.domain.CreditBureauRepository;
import org.apache.fineract.CreditCheck.domain.OrganisationCreditBureau;
import org.apache.fineract.CreditCheck.domain.OrganisationCreditBureauRepository;
import org.apache.fineract.CreditCheck.serialization.CBCommandFromApiJsonDeserializer;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrganisationCreditBureauWritePlatflormServiceImpl implements OrganisationCreditBureauWritePlatflormService
{

  private final PlatformSecurityContext context;
    
  private final OrganisationCreditBureauRepository orgCbRepository;
    
    private final CreditBureauRepository cbRepository;
    
   private final CBCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    
    @Autowired
    public OrganisationCreditBureauWritePlatflormServiceImpl(final PlatformSecurityContext context,final OrganisationCreditBureauRepository orgCbRepository,
            final CreditBureauRepository cbRepository,final CBCommandFromApiJsonDeserializer fromApiJsonDeserializer)
    {
      this.context=context;
      this.orgCbRepository=orgCbRepository;
      this.cbRepository=cbRepository;
      
      this.fromApiJsonDeserializer=fromApiJsonDeserializer;
      
    }
    
    @Override
    public CommandProcessingResult addOrgCreditBureau(Long ocb_id, JsonCommand command) {
        this.context.authenticatedUser();
         this.fromApiJsonDeserializer.validateForCreate(command.json(),ocb_id);
          
           final CreditBureau cb = this.cbRepository.getOne(ocb_id);
          
        final OrganisationCreditBureau orgcb=OrganisationCreditBureau.fromJson(command, cb);
          
          this.orgCbRepository.save(orgcb);
          
          return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(orgcb.getId()).build();
    }
    
    @Transactional
    @Override
    public CommandProcessingResult updateCreditBureau(JsonCommand command)
    {
      //  this.context.authenticatedUser();
       // this.fromApiJsonDeserializer.validateForCreate(command.json());
        
        final long creditbureauID = command.longValueOfParameterNamed("cb_id");
        System.out.println("creditbureauID is "+creditbureauID); 
        
        final boolean is_active=command.booleanPrimitiveValueOfParameterNamed("is_active");
        
        final OrganisationCreditBureau orgcb=orgCbRepository.getOne(creditbureauID);
        
        orgcb.setIs_active(is_active);
        
         orgCbRepository.saveAndFlush(orgcb);
        
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).withEntityId(orgcb.getId()).build();
        
    }

}
