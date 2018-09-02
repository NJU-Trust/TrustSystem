package nju.trust.dao.target;

import nju.trust.entity.CreditRating;
import nju.trust.entity.target.LargeProjectClassification;
import nju.trust.entity.target.LargeTarget;
import nju.trust.payloads.target.LargeTargetFilterRequest;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Author: J.D. Liao
 * Date: 2018/8/27
 * Description:
 */
public class LargeTargetSpecification implements Specification<LargeTarget> {

    private LargeTargetFilterRequest filter;

    public LargeTargetSpecification(LargeTargetFilterRequest filter) {
        this.filter = filter;
    }

    @Override
    public Predicate toPredicate(Root<LargeTarget> root, CriteriaQuery<?> criteriaQuery,
                                 CriteriaBuilder builder) {

        List<Predicate> predicates = new ArrayList<>();

        // Money
        Expression<Double> moneyExpression = root.get("money");
        Optional.ofNullable(filter.getMoney()[0])
                .ifPresent(t -> builder.ge(moneyExpression, t));
        Optional.ofNullable(filter.getMoney()[1])
                .ifPresent(t -> builder.le(moneyExpression, t));

        // Interest rate
        Expression<Double> interestRateExpression = root.get("repayment").get("yearInterestRate");
        Optional.ofNullable(filter.getInterestRate()[0])
                .ifPresent(t -> predicates.add(builder.ge(interestRateExpression, t)));
        Optional.ofNullable(filter.getInterestRate()[1])
                .ifPresent(t -> predicates.add(builder.le(interestRateExpression, t)));

        // Repayment duration
        Expression<Integer> durationExpression = root.get("repayment").get("repaymentDuration");
        Optional.ofNullable(filter.getRepaymentDuration()[0])
                .ifPresent(t -> predicates.add(builder.ge(durationExpression, t)));
        Optional.ofNullable(filter.getRepaymentDuration()[1])
                .ifPresent(t -> predicates.add(builder.le(durationExpression, t)));

        // Start time
        Expression<LocalDateTime> timeExpression = root.get("startTime");
        Optional.ofNullable(filter.getTime()[0])
                .ifPresent(t -> predicates.add(builder.greaterThanOrEqualTo(timeExpression, t)));
        Optional.ofNullable(filter.getTime()[1])
                .ifPresent(t -> predicates.add(builder.lessThanOrEqualTo(timeExpression, t)));

        // User failed subjects
        Expression<Integer> failedSubjectsExpression = root.get("user").get("failedSubjects");
        Optional.ofNullable(filter.getUserFailedSubject()[0])
                .ifPresent(t -> predicates.add(builder.ge(failedSubjectsExpression, t)));
        Optional.ofNullable(filter.getUserFailedSubject()[1])
                .ifPresent(t -> predicates.add(builder.le(failedSubjectsExpression, t)));

        // User ranking rate
        Expression<Double> rankingExpression = root.get("user").get("rankingRate");
        Optional.ofNullable(filter.getUserRankingRate()[0])
                .ifPresent(t -> predicates.add(builder.ge(rankingExpression, t)));
        Optional.ofNullable(filter.getUserRankingRate()[1])
                .ifPresent(t -> predicates.add(builder.le(rankingExpression, t)));

        Predicate[] p = new Predicate[predicates.size()];
        Predicate result = builder.and(predicates.toArray(p));

        for (CreditRating creditRating : filter.getUserCreditRating())
            result = builder.or(result, builder.equal(root.get("user").get("creditRating"), creditRating));
        for (LargeProjectClassification classification : filter.getClassifications())
            result = builder.or(result, builder.equal(root.get("classification"), classification));

        return result;
    }
}
