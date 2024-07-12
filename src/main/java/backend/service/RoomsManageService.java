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
        return List.of(new RoomsInfo());
    }
}
