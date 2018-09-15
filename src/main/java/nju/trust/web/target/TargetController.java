package nju.trust.web.target;

import nju.trust.entity.user.RepaymentType;
import nju.trust.payloads.ApiResponse;
import nju.trust.payloads.Range;
import nju.trust.payloads.investment.InterestRateInterval;
import nju.trust.payloads.investment.InvestmentStrategy;
import nju.trust.payloads.target.*;
import nju.trust.service.UserService;
import nju.trust.service.target.TargetService;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;

/**
 * Author: J.D. Liao
 * Date: 2018/8/24
 * Description:
 */
@RestController
@RequestMapping("/loan")
public class TargetController {

    private TargetService targetService;

    private UserService userService;

    public TargetController(TargetService targetService, UserService userService) {
        this.targetService = targetService;
        this.userService = userService;
    }

    @RequestMapping("/details")
    public TargetDetails targetDetails(Long targetId) {
        return targetService.getTargetDetails(targetId);
    }

    @PostMapping("/new/small")
    public ApiResponse createSmallTarget(SmallTargetRequest smallTargetRequest, Principal principal) {
        return targetService.applySmallTarget(smallTargetRequest, principal.getName());
    }

    @PostMapping("/new/large")
    public ApiResponse createLargeTarget(LargeTargetRequest largeTargetRequest, Principal principal) {
        return targetService.applyLargeTarget(largeTargetRequest, principal.getName());
    }

    @RequestMapping("/interestRate")
    public InterestRateInterval getInterestRateInterval() {
        return null;
    }

    @RequestMapping("/largeTargetList")
    public List<TargetInfo> getLargeTargets(Pageable pageable, @Valid LargeTargetFilterRequest filter) {
        return targetService.filterLargeTargets(pageable, filter);
    }

    @RequestMapping("/smallTargetList")
    public List<TargetInfo> getSmallTargets(Pageable pageable, SmallTargetFilterRequest filter) {
        System.out.println(pageable);
        System.out.println(filter);
        return targetService.filterSmallTargets(pageable, filter);
    }

    @RequestMapping("/recommendSmall")
    public List<TargetInfo> smallTargetRecommendation(@Valid SmallTargetFilterRequest filterRequest) {
        return targetService.recommendSmallTargets(filterRequest);
    }

    @RequestMapping("/recommendStrategy")
    public List<InvestmentStrategy> getRecommendationStrategy(@RequestParam("targets") List<Integer> targets,
                                                              Double expectedInterestRate, Double money) {
        return targetService.recommendStrategy(targets, money, expectedInterestRate);
    }

    @RequestMapping("/rateRange")
    public Range<Double> getInterestRateRange(Principal principal) {
        return userService.getInterestRange(principal.getName());
    }

    @RequestMapping("/timeRange")
    public Range<Double> getLoanTimeRange(Principal principal, double money) {
        return targetService.getLoanTimeRange(principal.getName(), money);
    }

    @RequestMapping("/remainAmount")
    public Double getRemainingAmount(Principal principal) {
        return targetService.getRemainingAmount(principal.getName());
    }

    @RequestMapping("/repayment/ep")
    public RepaymentTotalInfo getEPRepaymentInfo(Principal principal, RepaymentRequest repaymentRequest) {
        return targetService.getRepaymentInfo(principal.getName(), RepaymentType.EQUAL_PRINCIPAL,
                repaymentRequest.getMoney(), repaymentRequest.getDuration(), repaymentRequest.getInterestRate());
    }

    @RequestMapping("/repayment/eipi")
    public RepaymentTotalInfo getEIPIRepaymentInfo(Principal principal, RepaymentRequest repaymentRequest) {
        return targetService.getRepaymentInfo(principal.getName(),
                RepaymentType.EQUAL_INSTALLMENT_OF_PRINCIPAL_AND_INTEREST,
                repaymentRequest.getMoney(), repaymentRequest.getDuration(), repaymentRequest.getInterestRate());
    }

    @RequestMapping("/repayment/otp")
    public RepaymentTotalInfo getOTPRepaymentInfo(Principal principal, RepaymentRequest repaymentRequest) {
        return targetService.getRepaymentInfo(principal.getName(), RepaymentType.ONE_TIME_PAYMENT,
                repaymentRequest.getMoney(), repaymentRequest.getDuration(), repaymentRequest.getInterestRate());
    }

    @RequestMapping("/repayment/pi")
    public RepaymentTotalInfo getPIRepaymentInfo(Principal principal, RepaymentRequest repaymentRequest) {
        return targetService.getRepaymentInfo(principal.getName(), RepaymentType.PRE_INTEREST,
                repaymentRequest.getMoney(), repaymentRequest.getDuration(), repaymentRequest.getInterestRate());
    }

    @RequestMapping("/repayment/consumptionCorrection")
    public ConsumptionCorrection getConsumptionCorrection(Principal principal, Long targetId) {
        return targetService.getConsumptionCorrection(principal.getName(), targetId);
    }
}
