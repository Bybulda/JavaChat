package backend.repository;

import backend.model.MessagesInfo;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessagesInfoRepository extends CrudRepository<MessagesInfo, Long> {
    public List<MessagesInfo> findAllByChatId(Long chatId);

    public void deleteByIdAndChatId(Long id, Long chatId);

    public void deleteAllByChatId(Long chatId);
}
