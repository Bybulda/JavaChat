package backend.service;

import backend.model.CipherInfo;
import backend.repository.CipherInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CipherManageService {
    @Autowired
    private CipherInfoRepository cipherInfoRepository;

    public CipherInfo getCipherInfo(Long id) {
        return cipherInfoRepository.findCipherInfoById(id);
    }

    public void deleteCipherInfo(Long id) {
        cipherInfoRepository.deleteCipherInfoById(id);
    }

    public void saveCipherInfo(CipherInfo cipherInfo) {
        cipherInfoRepository.save(cipherInfo);
    }
}
