package nju.trust.service.personalinfo;

import nju.trust.dao.admin.BaseTargetRepository;
import nju.trust.dao.admin.RepaymentRepository;
import nju.trust.dao.admin.UnstructuredDataRepository;
import nju.trust.dao.admin.UserEvidenceDao.UserEvidenceRepository;
import nju.trust.dao.admin.UserInfoCheckRecordRepository;
import nju.trust.dao.record.InvestmentRecordRepository;
import nju.trust.dao.record.RepaymentRecordRepository;
import nju.trust.dao.user.UserMonthlyStatisticsRepository;
import nju.trust.dao.user.UserRepository;
import nju.trust.entity.CheckItem;
import nju.trust.entity.CheckState;
import nju.trust.entity.UserLevel;
import nju.trust.entity.record.InvestmentRecord;
import nju.trust.entity.record.RepaymentRecord;
import nju.trust.entity.record.UserEvidence.*;
import nju.trust.entity.record.UserInfoCheckRecord;
import nju.trust.entity.target.BaseTarget;
import nju.trust.entity.target.TargetState;
import nju.trust.entity.target.TargetType;
import nju.trust.entity.user.*;
import nju.trust.payloads.personalinfomation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author: 161250127
 * @Description:
 * @Date: 2018/9/11
 */
@Service
public class PersonalInformationServiceImpl implements PersonalInformationService {
    private UserRepository userRepository;
    private InvestmentRecordRepository investmentRecordRepository;
    private UnstructuredDataRepository unstructuredDataRepository;
    private BaseTargetRepository baseTargetRepository;
    private UserEvidenceRepository userEvidenceRepository;
    private UserInfoCheckRecordRepository userInfoCheckRecordRepository;
    private RepaymentRepository repaymentRepository;
    private UserMonthlyStatisticsRepository userMonthlyStatisticsRepository;
    private RepaymentRecordRepository repaymentRecordRepository;

    private static final String noComplete = "未填写";

    @Autowired
    public PersonalInformationServiceImpl(UserRepository userRepository, InvestmentRecordRepository investmentRecordRepository, UnstructuredDataRepository unstructuredDataRepository, BaseTargetRepository baseTargetRepository, UserEvidenceRepository userEvidenceRepository, UserInfoCheckRecordRepository userInfoCheckRecordRepository, RepaymentRepository repaymentRepository, UserMonthlyStatisticsRepository userMonthlyStatisticsRepository, RepaymentRecordRepository repaymentRecordRepository) {
        this.userRepository = userRepository;
        this.investmentRecordRepository = investmentRecordRepository;
        this.unstructuredDataRepository = unstructuredDataRepository;
        this.baseTargetRepository = baseTargetRepository;
        this.userEvidenceRepository = userEvidenceRepository;
        this.userInfoCheckRecordRepository = userInfoCheckRecordRepository;
        this.repaymentRepository = repaymentRepository;
        this.userMonthlyStatisticsRepository = userMonthlyStatisticsRepository;
        this.repaymentRecordRepository = repaymentRecordRepository;
    }

    // 投资借款部分
    @Override
    public InvestAndLoan getInvestAndLoanInfo(String username) {
        User user = userRepository.findByUsername(username).get();
        InvestAndLoan info = new InvestAndLoan();
        // 投资总额
        double totalInvestment = calTotalInvestment(username);
        info.setTotalInvestment(toForm(totalInvestment));
        // 借款总额
        double totalLoan = calTotalLoan(username);
        info.setTotalLoan(toForm(totalLoan));
        // 待收回本息
        double getMoney = calGetMoney(username);
        info.setGetMoney(toForm(getMoney));
        // 本息收回进度
        double totalMoney = calPI(username);  // 本息
        double getMoneyRrogress = 100;
        if(totalMoney != 0) {
            getMoneyRrogress = getMoney / totalMoney * 100;
        }
        info.setGetMoneyProgress(toForm(getMoneyRrogress));
        // 待偿还本息
        double payMoney = calPayMoney(username);
        info.setPayMoney(toForm(payMoney));
        // 本息偿还进度
        double payMoneyProgress = 100;
        if(totalLoan != 0) {
            payMoneyProgress = payMoney / totalLoan * 100;
        }
        info.setPayMoneyProgress(toForm(payMoneyProgress));
        // 信用评分
        info.setCreditRating(user.getCreditRating().toString());
        // 信用评级
        info.setCreditRatingScore(user.getCreditScore());
        return info;
    }
    // 本息
    private double calPI(String username) {
        double sum = 0;

        List<InvestmentRecord> investmentRecords = investmentRecordRepository.findAllByUserUsername(username);
        if(investmentRecords == null) {
            return sum;
        }
        for(InvestmentRecord record : investmentRecords) {
            long targetId = record.getTarget().getId();
            Repayment repayment = repaymentRepository.findFirstByTargetId(targetId);
            sum = sum + record.getInvestedMoney() * repayment.getInterestRate() * repayment.getDuration() / 12;
        }

        return sum;
    }
    // 计算投资总额
    private double calTotalInvestment(String username) {
        List<InvestmentRecord> records = investmentRecordRepository.findAllByUserUsername(username);
        double sum = 0;
        if (records == null) {
            return sum;
        }
        for(InvestmentRecord record : records) {
            sum = sum + record.getInvestedMoney();
        }
        return sum;
    }
    // 借款总额
    private double calTotalLoan(String username) {
        List<BaseTarget> targets = baseTargetRepository.findDistinctByUserUsername(username);
        double sum = 0;
        if(targets == null) {
            return sum;
        }
        for(BaseTarget target : targets) {
            sum = sum + target.getMoney();
        }
        return sum;
    }
    // 待收回本息
    private double calGetMoney(String username) {
        List<InvestmentRecord> investmentRecords = investmentRecordRepository.findDistinctByUserUsername(username);
        double sum = 0;
        if(investmentRecords == null) {
            return sum;
        }
        for(InvestmentRecord record : investmentRecords) {
            BaseTarget target = record.getTarget();
            double investedMoney = record.getInvestedMoney();
            Repayment repayment = repaymentRepository.findFirstByTargetId(target.getId());
            double remaining = repayment.getRemainingAmount();
            if(repayment == null) {
                System.out.println("targetId:"+target.getId()+"  repayment == null");
                continue;
            }else {
                double money = investedMoney<remaining ? investedMoney : remaining;
                sum = sum + money;
            }
        }
        return sum;
    }
    // 待偿还本息
    private double calPayMoney(String username) {
        double sum = 0;
        List<Repayment> repayments = repaymentRepository.findAllByUserUsername(username);
        if(repayments == null) {
            return sum;
        }
        for(Repayment repayment : repayments) {
            sum = repayment.getRemainingAmount() + sum;
        }
        return sum;
    }
    // 保留两位小数 xx.xx
    private double toForm(double num) {
        return Double.parseDouble(String.format("%.2f",num));
    }


    /**
     * 账户总额
     * @param username 用户名
     */
    @Override
    public TotalAccountInfo getTotalAccountInfo(String username) {
        TotalAccountInfo info = new TotalAccountInfo();

        double balance = 0;
        double frozenAmount = calFrozenAmount(username);
        double pendingPI = calPendingPI(username);
        double investmentInBidding = calInvestmentInBidding(username);
        double totalAccount = balance + frozenAmount + pendingPI + investmentInBidding;

        info.setTotalAccount(totalAccount);
        info.setBalance(balance);
        info.setFrozenAmount(frozenAmount);
        info.setPendingPI(pendingPI);
        info.setInvestmentInBidding(investmentInBidding);

        return info;
    }
    // 冻结金额:投资出去还没成交的部分（投资人视角，成交后转入待回收本息）
    private double calFrozenAmount(String username) {
        double sum = 0;

        List<InvestmentRecord> investmentRecords = investmentRecordRepository.findAllByUserUsername(username);
        if(investmentRecords == null) {
            return sum;
        }
        for(InvestmentRecord investment : investmentRecords) {
            if(investment.getTarget().getTargetState().equals(TargetState.ON_GOING)) { // 招标中
                sum = sum + investment.getInvestedMoney();
            }
        }

        return sum;
    }
    // 待回收本息:成交后的冻结金额（投资人视角）
    private double calPendingPI(String username) {
        return calGetMoney(username);
    }
    // 招标中投资:借款有人投进来但标的还没成交的
    private double calInvestmentInBidding(String username) {
        double sum = 0;

        List<BaseTarget> targets = baseTargetRepository.findDistinctByUserUsername(username);
        if(targets == null) {
            return sum;
        }
        for(BaseTarget target : targets) {
            if(target.getTargetState().equals(TargetState.ON_GOING)) {
                sum = sum + target.getCollectedMoney();
            }
        }

        return sum;
    }

    /**
     * 待办列表
     * @param username 用户名
     */
    @Override
    public List<EventsInfo> getAllEventsInfo(String username) {
        List<EventsInfo> events1 = getToPay(username);
        List<EventsInfo> events2 = getHasReceivrd(username);
        events1.addAll(events2);
        return events1;
    }
    // 得到未来一周要付款的信息
    private List<EventsInfo> getToPay(String username) {
        List<Repayment> repayments = repaymentRepository.findAllByUserUsername(username);
        List<EventsInfo> eventsInfos = new ArrayList<>();
        if(repayments == null) {
            return eventsInfos;
        }
        for(Repayment repayment : repayments) {
            LocalDate payDate = toPay(repayment);
            if(payDate != null) {
                EventsInfo info = new EventsInfo();
                info.setDate(toDateForm(payDate));
                info.setTitle(EventType.HAVEPAY.getStr());
                info.setDescription(getDescription(payDate, EventType.HAVEPAY));
                eventsInfos.add(info);
            }
        }

        Collections.sort(eventsInfos);
        return eventsInfos;
    }
    // 判断还款时间（若一周内不还款，null）
    private LocalDate toPay(Repayment repayment) {
        if(repayment.getRemainingAmount() <= 0) {   // 还完
            return null;
        }
        LocalDate payDate = repayment.getStartDate();
        int duration = repayment.getDuration();
        LocalDate now = LocalDate.now();
        LocalDate last = payDate.plusMonths(duration);
        if(now.isAfter(last)) { // 过了期限
            return null;
        }

        payDate = now.withDayOfMonth(last.getDayOfMonth());
        now = LocalDate.now();
        LocalDate define = now.plusDays(7);
        if(define.isBefore(payDate)) {
            return null;
        }
        return payDate;
    }
    // 得到收款信息
    private List<EventsInfo> getHasReceivrd(String username) {
        List<EventsInfo> eventsInfos = new ArrayList<>();
        // 得到用户的投资记录
        List<InvestmentRecord> investmentRecords = investmentRecordRepository.findAllByUserUsername(username);
        if(investmentRecords == null) {
            return eventsInfos;
        }
        for(InvestmentRecord record : investmentRecords) {
            BaseTarget target = record.getTarget();
            if(!target.getTargetState().equals(TargetState.IN_THE_PAYMENT)) {
                continue;
            }
            Long targetId = target.getId();
            Repayment repayment = repaymentRepository.findFirstByTargetId(targetId);
            LocalDate receiveDate = getReceivedDate(repayment);
            if(receiveDate == null) {
                continue;
            }
            EventsInfo info = new EventsInfo();
            info.setDate(toDateForm(receiveDate));
            info.setTitle(EventType.HAVEGOT.getStr());
            info.setDescription(getDescription(receiveDate, EventType.HAVEGOT));
            eventsInfos.add(info);
        }

        Collections.sort(eventsInfos);
        Collections.reverse(eventsInfos);
        return eventsInfos;
    }
    // 得到距离最近的收款日期
    private LocalDate getReceivedDate(Repayment repayment) {
        LocalDate now = LocalDate.now();
        LocalDate date = repayment.getStartDate();
        int duration = repayment.getDuration();

        for(int i = 1 ; i <= duration ; i++) {
            if(date.plusMonths(i).isAfter(now)) {
                if(i == 1) {
                    return null;
                }
                return date.plusMonths(i-1);
            }
        }
        return null;
    }
    // 日期格式：2018/9/8
    private String toDateForm(LocalDate date) {
        String seperator = "/";
        int day = date.getDayOfMonth();
        int month = date.getMonthValue();
        int year = date.getYear();
        return year+seperator+month+seperator+day;
    }
    // 得到description
    private String getDescription(LocalDate date, EventType type) {
        String str = "您有一笔";
        switch (type) {
            case HAVEGOT:str = str + "收款在";  break;
            case HAVEPAY:str = str + "还款在";  break;
        }
        String day = date.getMonthValue()+"月"+date.getDayOfMonth()+"日";
        return str+day;
    }

    /**
     * 校园表现
     * @param username 用户名
     */
    @Override
    public CampusPerformance getCampusPerformance(String username) {
        // 学校、学历、社交情况、获奖情况、成绩
        List<UnstructuredDataType> types = Arrays.asList(UnstructuredDataType.SCHOOL, UnstructuredDataType.EDUCATION, UnstructuredDataType.SOCIALITY, UnstructuredDataType.AWARD, UnstructuredDataType.GRADE);
        List<Double> personalPerformance = new ArrayList<>();
        List<Double> averagePerformance = new ArrayList<>();

        for(UnstructuredDataType type : types) {
            double personal = getPersonalPerformance(username, type);
            personalPerformance.add(personal);
            double average = getAveragePerformance(type);
            averagePerformance.add(average);
        }

        CampusPerformance performance = new CampusPerformance();
        performance.setPersonalPerformance(personalPerformance);
        performance.setAveragePerformance(averagePerformance);
        performance.setAboveAverage(calArea(personalPerformance) > calArea(averagePerformance));

        return performance;
    }
    // 得到个人表现
    private double getPersonalPerformance(String username, UnstructuredDataType type) {
        UnstructuredData unstructuredData = unstructuredDataRepository.findFirstByUserUsernameAndDataType(username, type);
        if(unstructuredData == null) {
            return type.getInitialScore();
        }
        return unstructuredData.getScore();
    }
    // 得到平台表现
    private double getAveragePerformance(UnstructuredDataType type) {
        int userNum = ((List)userRepository.findAll()).size()-1;
        List<UnstructuredData> dataList = unstructuredDataRepository.findDistinctByDataType(type);
        double sum = 0;
        for(UnstructuredData data : dataList) {
            sum = sum + data.getScore();
        }
        return sum/userNum;
    }
    // 计算面积
    private double calArea(List<Double> record) {
        double area = 0;
        for(int i = 0 ; i < record.size() ; i++) {
            for (int j = i+1 ; j < record.size() ; j++) {
                area = area + record.get(i)*record.get(j);
            }
        }
        return area;
    }

    /**
     * 信息表
     * @param username 用户名
     */
    @Override
    public PersonalDetailInfomation getPersonalDetailInformation(String username) {
        String schoolType = getSchoolType(username);
        String majorType = getMajorType(username);
        String educationType = getEducationType(username);
        String financeSource = getFinanceSource(username);
        String studyRank = getStudyRank(username);
        int noPass = getNoPass(username);
        List<String> scholarship = getScholarship(username);
        List<String> researchCompetition = getResearchCompetition(username);
        List<String> awards = getAwards(username);
        List<String> punishment = getPunishment(username);
        int payment = getPayment(username);
        int library = getLibrary(username);
        int cheating = getCheating(username);

        PersonalDetailInfomation info = new PersonalDetailInfomation();
        info.setSchoolClass(schoolType);
        info.setMajorCondition(majorType);
        info.setEducationBackground(educationType);
        info.setFinanceSource(financeSource);
        info.setStudyRank(studyRank);
        info.setNumNoPass(noPass);
        info.setScholarship(scholarship);
        info.setResearchCompetition(researchCompetition);
        info.setAwards(awards);
        info.setPunishment(punishment);
        info.setPayment(payment);
        info.setLibrary(library);
        info.setCheating(cheating);

        return info;
    }
    // 学校分类
    private String getSchoolType(String username) {
        List<SchoolEvidence> schoolEvidence = userEvidenceRepository.findSchoolEvidenceByUser(username);
        if(schoolEvidence == null || schoolEvidence.size() == 0) {
            return noComplete;
        }
        return schoolEvidence.get(schoolEvidence.size()-1).getSchoolType().getStr();
    }
    // 专业情况
    private String getMajorType(String username) {
        List<MajorEvidence> majorEvidences = userEvidenceRepository.findMajorEvidenceByUser(username);
        if(majorEvidences == null || majorEvidences.size() == 0) {
            return noComplete;
        }
        return majorEvidences.get(majorEvidences.size()-1).getMajorType().getStr();
    }
    // 受教育情况
    private String getEducationType(String username) {
        List<EducationEvidence> educationEvidences = userEvidenceRepository.findEducationEvidenceByUser(username);
        if(educationEvidences == null || educationEvidences.size() == 0) {
            return noComplete;
        }
        return educationEvidences.get(educationEvidences.size()-1).getEducationType().getStr();
    }
    // 经济来源
    private String getFinanceSource(String username) {
        UnstructuredData data = unstructuredDataRepository.findFirstByUserUsernameAndDataType(username, UnstructuredDataType.ECONOMIC);
        if(data == null || data.getDescription() == null || data.getDescription().length() == 0) {
            return noComplete;
        }
        return data.getDescription();
    }
    // 学习成绩
    private String getStudyRank(String username) {
        List<StudyEvidence> studyEvidences = userEvidenceRepository.findStudyEvidenceByUser(username);
        if(studyEvidences == null || studyEvidences.size() == 0) {
            return noComplete;
        }
        double rank = studyEvidences.get(studyEvidences.size()-1).getRanking();
        rank = (int)(rank*100)/100.0;
        return rank+"%";
    }
    // 挂科数
    private int getNoPass(String username) {
        List<FailEvidence> failEvidences = userEvidenceRepository.findFailEvidenceByUser(username);
        if(failEvidences == null || failEvidences.size() == 0) {
            return 0;
        }
        return failEvidences.get(0).getNum();
    }
    // 奖学金
    private List<String> getScholarship(String username) {
        return getStrRecord(username, CheckItem.SCHOLARSHIP);
    }
    // 科研竞赛获奖情况
    private List<String> getResearchCompetition(String username) {
        return getStrRecord(username, CheckItem.SCHOLARSHIP);
    }
    // 奖励情况
    private List<String> getAwards(String username) {
        return getStrRecord(username, CheckItem.REWARD);
    }
    // 惩罚情况
    private List<String> getPunishment(String username) {
        return getStrRecord(username, CheckItem.PUNISHMENT);
    }
    // 学费及住宿费缴纳状况
    private int getPayment(String username) {
        return getStrRecord(username, CheckItem.PAYMENT).size();
    }
    // 图书馆借阅还书情况
    private int getLibrary(String username) {
        return getStrRecord(username, CheckItem.RETURNBOOKS).size();
    }
    // 考试作弊的信息
    private int getCheating(String username) {
        return getStrRecord(username, CheckItem.TESTCHEAT).size();
    }
    private List<String> getStrRecord(String username, CheckItem item) {
        List<UserInfoCheckRecord> records = userInfoCheckRecordRepository.findDistinctByUserUsernameAndAndCheckItem(username, item);
        List<String> scholarships = new ArrayList<>();
        if(records == null) {
            return scholarships;
        }
        for(UserInfoCheckRecord record : records) {
            if(record.getCheckState().equals(CheckState.PASS)) {
                scholarships.add(record.getDescription());
            }
        }
        return scholarships;}

    /**
     * 校园关系图
     * @param username 用户名
     * TODO
     * 关系（信用交叉检验问卷第一问）：1-好友；2-普通同学；其他不考虑
     */
    @Override
    public PersonalRelationship getPersonalRelationships(String username) {
        List<String> usernameList = getRelationUsernames(username);

        List<People> peoples = new ArrayList<>();
        List<Relation> relations = new ArrayList<>();

        peoples.add(getPeople(username));
        for(String name : usernameList) {
            People people = getPeople(name);
            Relation relation = getRelation(username, name);
            peoples.add(people);
            relations.add(relation);
        }

        PersonalRelationship personalRelationship = new PersonalRelationship();
        personalRelationship.setPeople(peoples);
        personalRelationship.setRelations(relations);

        return personalRelationship;
    }
    // TODO 得到相关人员的用户名
    // 关系（信用交叉检验问卷第一问）：1-好友；2-普通同学；其他不考虑
    private List<String> getRelationUsernames(String username) {
        List<User> userList = (List<User>)userRepository.findAll();
        int i = 0;
        while(i < userList.size()) {
            String name = userList.get(i).getUsername();
            if(name.equals(username) || name.toLowerCase().equals("admin")) {
                userList.remove(i);
            }else {
                i++;
            }
        }

        int j = userList.size();
        j = j<5 ? j : 5;
        List<String> usernameList = new ArrayList<>();
        if(j == 0) {
            return new ArrayList<>();
        }
        ArrayList<Integer> indexs = new ArrayList<>();
        for(i = 0 ; i < j; i++) {
            int index = (int)Math.random()*userList.size();
            if(indexs.contains(index)) {
                continue;
            }
            String name = userList.get(index).getUsername();
            usernameList.add(name);
            indexs.add(index);
        }
        return usernameList;
    }
    private People getPeople(String username) {
        People people = new People();
        people.setName(username);
        people.setCreditPts(getCreditScore(username));
        people.setFinancialPts(toForm(getFinancialScore()));
        people.setSchoolPts(toForm(getCampusPerformanceScore(username)));

        return people;
    }
    // TODO
    private Relation getRelation(String source, String target) {
        Relation relation = new Relation();
        relation.setSource(source);
        relation.setTarget(target);
        relation.setName("同学");
        CreditChange creditChange = getCreditChange(target);
        relation.setCreditChange(creditChange.getStr());
        return relation;
    }
    // 信用得分
    private double getCreditScore(String username) {
        User user = userRepository.findByUsername(username).get();
        return user.getCreditScore();
    }
    // TODO 经济得分
    private double getFinancialScore() {
        double score = Math.random()*30 + 70;
        return toForm(score);
    }
    // 校园表现得分
    private double getCampusPerformanceScore(String username) {
        List<UnstructuredData> unstructuredDataList = unstructuredDataRepository.findDistinctByUserUsername(username);
        double score = 0;
        if(unstructuredDataList == null) {
            return score;
        }
        for(UnstructuredData data : unstructuredDataList) {
            if(data.getDataType().equals(UnstructuredDataType.VIOLATION) || data.getDataType().equals(UnstructuredDataType.FAILED_SUBJECTS) || data.getDataType().equals(UnstructuredDataType.CHEATING)) {
                score = score - data.getScore();
            }else {
                score = score + data.getScore();
            }
        }
        if(score < 0) {
            return 0;
        }else {
            return score/7;
        }
    }
    // TODO 信用变化情况
    private CreditChange getCreditChange(String username) {
        User user = userRepository.findByUsername(username).get();
        if(user.getUserLevel() != null && user.getUserLevel().equals(UserLevel.DISCREDIT)) {
            return CreditChange.FROZEN;
        }
        return CreditChange.getCreditChange();
    }

    /**
     * 数值分析
     * TODO 投资额结余
     * @param username   用户名
     * @param startMonth 开始月份
     * @param endMonth   结束月份
     * 月份格式：2018-1
     * 时间为闭区间
     */
    @Override
    public DataAnalysis getDataAnalysis(String username, String startMonth, String endMonth) {
        LocalDate start = getStart(startMonth);
        LocalDate end = getEnd(endMonth);

        List<MonthAnalysis> monthAnalysisList = new ArrayList<>();
        double incomeSum = 0, expenseSum = 0;   // 总收入,总支出
        LocalDate date = start;
        while(date.isBefore(end)) {
            List<UserMonthStatistics> userMonthStatisticsList = userMonthlyStatisticsRepository.findDistinctByUserUsernameAndDateBetween(username, date, date.with(TemporalAdjusters.lastDayOfMonth()));
            // 收入、支出、日常支出, 学习支出, 饮食支出，净资产，蚂蚁花呗
            double income = 0, expense = 0, daily = 0, learning = 0, food = 0, asset = 0, antCheckLater = 0;
            for(UserMonthStatistics userMonthStatistics : userMonthStatisticsList) {
                if(userMonthStatistics.getIncome() != null) {
                    income = income + userMonthStatistics.getIncome();
                }
                if(userMonthStatistics.getExpense() != null) {
                    expense = expense + userMonthStatistics.getExpense();
                }
                if(userMonthStatistics.getDaily() != null) {
                    daily = daily + userMonthStatistics.getDaily();
                }
                if(userMonthStatistics.getLearning() != null) {
                    learning = learning + userMonthStatistics.getLearning();
                }
                if(userMonthStatistics.getFood() != null) {
                    food = food + userMonthStatistics.getFood();
                }
                if(userMonthStatistics.getAsset() != null) {
                    asset = asset + userMonthStatistics.getAsset();
                }
                if(userMonthStatistics.getAntCheckLater() != null) {
                    antCheckLater = antCheckLater + userMonthStatistics.getAntCheckLater();
                }
            }
            // 月刚性支出 = 月日常支出 + 月学习支出 + 月饮食支出
            double expense_rig = daily + learning + food;
            // 月可调支出 = 月总支出 - 月刚性支出
            double expense_disc = expense - expense_rig;
            if(expense_disc < 0) {
                expense_disc = 0;
            }

            MonthAnalysis monthAnalysis = new MonthAnalysis(date);
            monthAnalysis.setIncome(income);
            monthAnalysis.setAsset(asset);
            monthAnalysis.setExpense(expense);
            monthAnalysis.setExpense_rig(expense_rig);
            monthAnalysis.setExpense_disc(expense_disc);
            // TODO
            monthAnalysis.setSurplus(0);

            double lblt = antCheckLater + getToRepaySum(username, date);
            monthAnalysis.setLblt(lblt);

            monthAnalysisList.add(monthAnalysis);
            incomeSum = incomeSum + income;
            expenseSum = expenseSum + expense;

            date = date.plusMonths(1);
        }

        return new DataAnalysis(monthAnalysisList, incomeSum, expenseSum);
    }
    // 得到本月需偿还金额
    private double getToRepaySum(String username, LocalDate start) {
        double toRepaySum = 0;

        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
        start = start.with(TemporalAdjusters.firstDayOfMonth());

        List<RepaymentRecord> repaymentRecords = repaymentRecordRepository.findDistinctByUserUsernameAndReturnDateBetween(username, start, end);
        for(RepaymentRecord repaymentRecord : repaymentRecords) {
            toRepaySum = toRepaySum + repaymentRecord.getSum();
        }

        return toRepaySum;
    }
    private LocalDate getStart(String startMonth) {
        return getLocalDate(startMonth);
    }
    private LocalDate getEnd(String endMonth) {
        LocalDate firstDay = getLocalDate(endMonth);
        return firstDay.with(TemporalAdjusters.lastDayOfMonth());
    }
    private LocalDate getLocalDate(String month) {
        final String regex = "-";
        String[] strs = month.split(regex);
        int y = Integer.valueOf(strs[0]);
        int m = Integer.valueOf(strs[1]);
        LocalDate localDate = LocalDate.of(y, m, 1);
        return localDate;
    }

    /**
     * 趋势分析
     * @param username   用户名
     * @param startMonth 开始月份
     * @param endMonth   结束月份
     * 月份格式：2018-1
     * 时间为闭区间
     */
    @Override
    public List<TrendAnalysis> getTrendAnalysis(String username, String startMonth, String endMonth) {
        LocalDate start = getStart(startMonth);
        LocalDate end = getEnd(endMonth);

        List<TrendAnalysis> trendAnalysisList = new ArrayList<>();

        LocalDate date = start;
        while(date.isBefore(end)) {
            TrendAnalysis trendAnalysis = new TrendAnalysis(date);

            UserMonthStatistics userMonthStatistics = userMonthlyStatisticsRepository.findTopByUserUsernameAndDateBetween(username, date, date.with(TemporalAdjusters.lastDayOfMonth()));

            if(userMonthStatistics.getEngel() != null) {
                trendAnalysis.setEngel(userMonthStatistics.getEngel());
            }
            if(userMonthStatistics.getRigidRatio() != null) {
                trendAnalysis.setRig_ratio(userMonthStatistics.getRigidRatio());
            }
            if(userMonthStatistics.getDebtToAssetRatio() != null) {
                trendAnalysis.setD2a_ratio(userMonthStatistics.getDebtToAssetRatio());
            }
            if(userMonthStatistics.getDebtPayingAbility() != null) {
                trendAnalysis.setDp_ability(userMonthStatistics.getDebtPayingAbility());
            }
            if(userMonthStatistics.getLeverage() != null) {
                trendAnalysis.setLeverage(userMonthStatistics.getLeverage());
            }
            if(userMonthStatistics.getConsumptionRatio() != null) {
                trendAnalysis.setConsump_ratio(userMonthStatistics.getConsumptionRatio());
            }
            if(userMonthStatistics.getSavingRatio() != null) {
                trendAnalysis.setSaving_ratio(userMonthStatistics.getSavingRatio());
            }

            trendAnalysisList.add(trendAnalysis);
            date = date.plusMonths(1);
        }

        return trendAnalysisList;
    }
}
