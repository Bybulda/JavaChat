package application.backend.service;

import application.backend.model.CipherInfo;
import application.backend.repository.CipherInfoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CipherManageService {
    @Autowired
    private CipherInfoRepository cipherInfoRepository;

    public CipherInfo getCipherInfo(Long id) {
        return cipherInfoRepository.findCipherInfoById(id);
    }

    @Transactional
    public void deleteCipherInfo(Long id) {
        cipherInfoRepository.deleteCipherInfoById(id);
    }

    @Transactional
    public CipherInfo saveCipherInfo(CipherInfo cipherInfo) {
        return cipherInfoRepository.save(cipherInfo);
    }
}
