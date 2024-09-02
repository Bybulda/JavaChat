package application.backend.service;

import application.backend.model.MessagesInfo;
import application.backend.repository.MessagesInfoRepository;
import jakarta.transaction.Transactional;
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

    @Transactional
    public void deleteMessageById(Long messageId, Long chatId) {
        messagesInfoRepository.deleteByIdAndChatId(messageId, chatId);
    }

    @Transactional
    public void deleteAllMessagesByChatId(Long chatId) {
        messagesInfoRepository.deleteAllByChatId(chatId);
    }

    @Transactional
    public MessagesInfo saveMessage(MessagesInfo messagesInfo) {
        return messagesInfoRepository.save(messagesInfo);
    }
}
