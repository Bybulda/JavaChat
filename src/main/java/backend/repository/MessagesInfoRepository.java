package backend.repository;

import backend.model.MessagesInfo;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MessagesInfoRepository extends CrudRepository<MessagesInfo, Long> {
    public List<MessagesInfo> findAllByChatId(Long chatId);

    public void deleteByIdAndChatId(Long id, Long chatId);

    public void deleteAllByChatId(Long chatId);
}
