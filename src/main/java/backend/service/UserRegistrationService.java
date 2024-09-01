package backend.service;

import backend.model.UserInfo;
import backend.repository.UserInfoRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserRegistrationService {
    @Autowired
    private UserInfoRepository userInfoRepository;

    public Pair<Boolean, UserInfo> registerUser(String username, String password) {
        if (userInfoRepository.existsByUserName(username)) {
            Optional<UserInfo> foundUser = Optional.of(userInfoRepository.findUserInfoByUserNameAndPassword(username, password));
            return foundUser.map(userInfo -> Pair.of(true, userInfo)).orElseGet(() -> Pair.of(false, new UserInfo()));
        }
        userInfoRepository.save(UserInfo.builder().userName(username).password(password).build());
        return Pair.of(true, Optional.of(userInfoRepository.findUserInfoByUserNameAndPassword(username, password)).get());
    }

    public UserInfo getUserInfoByUsername(String username) {
        return userInfoRepository.findUserInfoByUserName(username);
    }

    @Transactional
    public void saveUserInfo(UserInfo userInfo) {
        userInfoRepository.save(userInfo);
    }
}
