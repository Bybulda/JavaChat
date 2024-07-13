package backend.repository;

import backend.model.RoomsInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface RoomsInfoRepository extends CrudRepository<RoomsInfo, Long> {
    public Optional<RoomsInfo> findByRoomId(Long roomId);
    public List<RoomsInfo> findAllByLeftUserOrRightUser(Long leftUserId, Long rightUserId);
    public boolean existsByLeftUserAndRightUserOrRightUserAndLeftUser(Long leftId, Long rightId, Long rightUserId, Long leftUserId);
}
