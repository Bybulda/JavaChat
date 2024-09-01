package backend.repository;

import backend.model.CipherInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CipherInfoRepository extends CrudRepository<CipherInfo, Long> {
    public CipherInfo findCipherInfoById(Long id);

    public void deleteCipherInfoById(Long id);
}
