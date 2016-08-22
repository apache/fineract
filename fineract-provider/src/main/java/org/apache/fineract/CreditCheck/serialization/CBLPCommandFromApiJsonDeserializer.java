package org.apache.fineract.CreditCheck.serialization;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

@Component
public class CBLPCommandFromApiJsonDeserializer 
{
    private final Set<String> supportedParameters = new HashSet<>(
            Arrays.asList("loan_product_id", "is_creditcheck_mandatory", "skip_creditcheck_in_failure", "stale_period", "is_active","locale"));
    
    private final FromJsonHelper fromApiJsonHelper;
    
    @Autowired
    public CBLPCommandFromApiJsonDeserializer(final FromJsonHelper fromApiJsonHelper)
    {
        this.fromApiJsonHelper=fromApiJsonHelper;
    }
    
    public void validateForCreate(final String json,final Long cb_id) {
        if (StringUtils.isBlank(json)) { throw new InvalidJsonException(); }
        
        
        final Type typeOfMap = new TypeToken<Map<String, Object>>() {}.getType();
        this.fromApiJsonHelper.checkForUnsupportedParameters(typeOfMap, json, this.supportedParameters);

        final List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        final DataValidatorBuilder baseDataValidator = new DataValidatorBuilder(dataValidationErrors).resource("CB_LP_Mapping");
        
        final JsonElement element = this.fromApiJsonHelper.parse(json);
        
      
        baseDataValidator.reset().value(cb_id).notBlank().integerGreaterThanZero();
        
        final long loan_product_id = this.fromApiJsonHelper.extractLongNamed("loan_product_id", element);
        baseDataValidator.reset().parameter("loan_product_id").value(loan_product_id).notBlank().integerGreaterThanZero();
        System.out.println("loan product id "+loan_product_id);
        
        final boolean is_creditcheck_mandatory = this.fromApiJsonHelper.extractBooleanNamed("is_creditcheck_mandatory", element);
        baseDataValidator.reset().parameter("is_creditcheck_mandatory").value(is_creditcheck_mandatory).notBlank().trueOrFalseRequired(is_creditcheck_mandatory);
        
        final boolean skip_creditcheck_in_failure = this.fromApiJsonHelper.extractBooleanNamed("skip_creditcheck_in_failure", element);
        baseDataValidator.reset().parameter("skip_creditcheck_in_failure").value(skip_creditcheck_in_failure).notBlank().trueOrFalseRequired(skip_creditcheck_in_failure);
        System.out.println("skip_credit_check_in_failure is "+skip_creditcheck_in_failure);
        
        final long stale_period = this.fromApiJsonHelper.extractLongNamed("stale_period", element);
        baseDataValidator.reset().parameter("stale_period").value(stale_period).notBlank().integerGreaterThanZero();
        
        
        
        Boolean is_active=this.fromApiJsonHelper.extractBooleanNamed("is_active", element);
        if (is_active==null)
        {
            is_active=false;
        }
        else
        {
           
            baseDataValidator.reset().parameter("is_active").value(is_active).notBlank().trueOrFalseRequired(is_active);  
        }
        
       
        
        throwExceptionIfValidationWarningsExist(dataValidationErrors);
        
        
    }
    
    private void throwExceptionIfValidationWarningsExist(final List<ApiParameterError> dataValidationErrors) {
        if (!dataValidationErrors.isEmpty()) { throw new PlatformApiDataValidationException("validation.msg.validation.errors.exist",
                "Validation errors exist.", dataValidationErrors); }
    }
}
