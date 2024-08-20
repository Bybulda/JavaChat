package backend.service;

import backend.model.MessagesInfo;
import backend.repository.MessagesInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessagesMenageService {
    @Autowired
    private MessagesInfoRepository messagesInfoRepository;

    public List<MessagesInfo> getMessagesByChatId(Long chatId) {
        return messagesInfoRepository.findAllByChatId(chatId);
    }

    public void deleteMessageById(Long messageId, Long chatId) {
        messagesInfoRepository.deleteByIdAndChatId(messageId, chatId);
    }

    public void deleteAllMessagesByChatId(Long chatId) {
        messagesInfoRepository.deleteAllByChatId(chatId);
    }
}
