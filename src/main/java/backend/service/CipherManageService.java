package backend.service;

import backend.model.CipherInfo;
import backend.repository.CipherInfoRepository;
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
    public void saveCipherInfo(CipherInfo cipherInfo) {
        cipherInfoRepository.save(cipherInfo);
    }
}
