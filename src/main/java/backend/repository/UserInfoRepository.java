package backend.repository;

import backend.model.UserInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {
    public boolean existsByUserName(String name);

    public UserInfo findUserInfoByUserNameAndPassword(String name, String password);

    public UserInfo findUserInfoByUserName(String name);

}
