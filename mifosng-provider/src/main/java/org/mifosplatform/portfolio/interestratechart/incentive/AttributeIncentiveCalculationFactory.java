package org.mifosplatform.portfolio.interestratechart.incentive;

public class AttributeIncentiveCalculationFactory {

    public static AttributeIncentiveCalculation findAttributeIncentiveCalculation(InterestIncentiveEntityType entityType) {
        AttributeIncentiveCalculation attributeIncentiveCalculation = null;
        switch (entityType) {
            case CUSTOMER:
                attributeIncentiveCalculation = new ClientAttributeIncentiveCalculation();
            break;
            default:
            break;
        }
        return attributeIncentiveCalculation;
    }

}
