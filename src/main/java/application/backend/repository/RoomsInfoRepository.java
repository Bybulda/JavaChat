package application.backend.repository;

import application.backend.model.RoomsInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomsInfoRepository extends CrudRepository<RoomsInfo, Long> {
    public List<RoomsInfo> findAllByLeftUserOrRightUser(Long leftUserId, Long rightUserId);

    public boolean existsByLeftUserAndRightUserOrRightUserAndLeftUser(Long leftId, Long rightId, Long rightUserId, Long leftUserId);

    public void deleteById(Long id);
}
