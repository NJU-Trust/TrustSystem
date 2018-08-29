package nju.trust.entity.target;

import nju.trust.entity.user.User;
import nju.trust.payloads.target.SmallTargetRequest;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("SMALL")
public class SmallTarget extends BaseTarget {

    private SmallProjectClassification classification;

    /**
     * 可承受额外费用偏好（希望为这笔融资至多付多少费用）
     */
    private Double maximumAmount;

    public SmallTarget(SmallTargetRequest request, User user) {
        super(request, user);
        maximumAmount = request.getMaximumAmount();
        classification = request.getClassification();
        identityOption = request.getIdentityOption();

        targetType = TargetType.SMALL;
    }

    @Override
    public String toString() {
        return "SmallTarget{" +
                "classification=" + classification +
                ", maximumAmount=" + maximumAmount +
                '}';
    }

    public SmallProjectClassification getClassification() {
        return classification;
    }

    public void setClassification(SmallProjectClassification classification) {
        this.classification = classification;
    }

    public Double getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(Double maximumAmount) {
        this.maximumAmount = maximumAmount;
    }
}
