package backend.service;

import backend.repository.CipherInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CipherManageService {
    @Autowired
    private CipherInfoRepository cipherInfoRepository;
}
