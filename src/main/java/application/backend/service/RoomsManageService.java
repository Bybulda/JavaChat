package application.backend.service;

import application.backend.model.RoomsInfo;
import application.backend.repository.RoomsInfoRepository;
import jakarta.transaction.Transactional;
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

    public boolean checkRoomInfoForTwoUsers(Long leftUserId, Long rightUserId){
        return roomsInfoRepository.existsByLeftUserAndRightUserOrRightUserAndLeftUser(leftUserId, rightUserId, leftUserId, rightUserId);
    }

    @Transactional
    public void deleteRoom(Long roomId){
        roomsInfoRepository.deleteById(roomId);
    }

    @Transactional
    public void saveRoom(RoomsInfo room){
        roomsInfoRepository.save(room);
    }
}
