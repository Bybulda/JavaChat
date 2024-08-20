package backend.service;

import backend.model.RoomsInfo;
import backend.repository.RoomsInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomsManageService {
    @Autowired
    private RoomsInfoRepository roomsInfoRepository;

    public List<RoomsInfo> getRoomsInfoForUser(Long userId){
        return roomsInfoRepository.findAllByLeftUserOrRightUser(userId, userId);
    }

    public boolean checkRoomInfoForTwoUsers(Long rightUserId, Long leftUserId){
        return roomsInfoRepository.existsByLeftUserAndRightUserOrRightUserAndLeftUser(leftUserId, rightUserId, rightUserId, leftUserId);
    }

    public void deleteRoom(Long roomId){
        roomsInfoRepository.deleteById(roomId);
    }
}
