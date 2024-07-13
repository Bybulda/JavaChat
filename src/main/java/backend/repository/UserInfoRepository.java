package backend.repository;

import backend.model.UserInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserInfoRepository extends CrudRepository<UserInfo, Long> {
    public boolean existsByName(String name);

    public Optional<UserInfo> findUserInfoByNameAndPassword(String name, String password);

    public Optional<Long> findIdByName(String name);
}
