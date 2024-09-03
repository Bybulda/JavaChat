package application.backend.service;

import application.backend.model.UserInfo;
import application.backend.repository.UserInfoRepository;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserRegistrationService {
    @Autowired
    private UserInfoRepository userInfoRepository;

    public Pair<Boolean, UserInfo> registerUser(String username, String password) {
        if(userInfoRepository.existsByUserNameAndPassword(username, password)) {
            return Pair.of(true, getUserInfoByUsername(username));
        }
        if (userInfoRepository.existsByUserName(username)){
            return Pair.of(false, getUserInfoByUsername(username));
        }
        userInfoRepository.save(UserInfo.builder().userName(username).password(password).build());
        return Pair.of(true, getUserInfoByUsername(username));


    }

    public UserInfo getUserInfoByUsername(String username) {
        return userInfoRepository.findUserInfoByUserName(username);
    }

    public boolean checkUserByUSerName(String username) {
        return userInfoRepository.existsByUserName(username);
    }

    @Transactional
    public void saveUserInfo(UserInfo userInfo) {
        userInfoRepository.save(userInfo);
    }
}
