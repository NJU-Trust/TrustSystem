package nju.trust.dao.user;

import nju.trust.entity.user.CreditCrossCheck;
import nju.trust.entity.user.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * @Author: 161250127
 * @Description:
 * @Date: 2018/10/21
 */
public interface UserCrossCheckRepository extends CrudRepository<CreditCrossCheck, Long> {

    List<CreditCrossCheck> findAllByRelatedPersonUsername(String username);

}
