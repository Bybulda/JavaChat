package application.backend.repository;

import application.backend.model.UserInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {
    public boolean existsByUserName(String name);

    public boolean existsByUserNameAndPassword(String username, String password);

    public UserInfo findUserInfoByUserNameAndPassword(String name, String password);

    public UserInfo findUserInfoByUserName(String name);

}
