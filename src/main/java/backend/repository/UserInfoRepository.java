package backend.repository;

import backend.model.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {
    public boolean existsByUserName(String name);

    public Optional<UserInfo> findUserInfoByUserNameAndPassword(String name, String password);

    public Optional<Long> findIdByUserName(String name);
}
