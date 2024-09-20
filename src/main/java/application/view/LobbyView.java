package application.view;

import application.backend.kafka.writer.KafkaWriter;
import application.backend.kafka.writer.KafkaWriterImpl;
import application.backend.model.CipherInfo;
import application.backend.model.MessagesInfo;
import application.backend.model.RoomsInfo;
import application.backend.model.UserInfo;
import application.backend.parser.JsonActionParser;
import application.backend.parser.JsonMessageParser;
import application.backend.parser.impl.JsonActionParserImpl;
import application.backend.parser.impl.JsonMessageParserImpl;
import application.backend.parser.model.JsonAction;
import application.backend.parser.model.JsonMessage;
import application.backend.service.CipherManageService;
import application.backend.service.MessagesMenageService;
import application.backend.service.RoomsManageService;
import application.backend.service.UserRegistrationService;
import application.view.extenders.NotificationHolder;
import cipher.ICipherService;
import cipher.algoritms.CipherService;
import cipher.diffie_hellman.DiffieHellman;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.*;
import com.vaadin.flow.server.StreamResource;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Route("lobby/:id/:name")
@PageTitle("Lobby")
@CssImport("./styles/styles.css")
public class LobbyView extends HorizontalLayout implements BeforeEnterObserver, NotificationHolder, BeforeLeaveObserver {
    private static final Logger log = LoggerFactory.getLogger(LobbyView.class);
    // Services
    @Autowired
    CipherManageService cipherManageService;
    @Autowired
    MessagesMenageService messagesMenageService;
    @Autowired
    RoomsManageService roomsManageService;
    @Autowired
    UserRegistrationService userRegistrationService;

    // region fields
    // user info
    private long id;
    private String userName;
    private volatile boolean isPersonDisconnected = true;
    private volatile boolean connectedMessageSent = false;

    private final UI currentUIChannel = UI.getCurrent();
    private final UI currentUIMessages = UI.getCurrent();
    // ui
    private final Grid<RoomsInfo> channelsGrid;
    private final VerticalLayout chatPlace;
    private final H3 header;
    // chat info
    private RoomsInfo currentRoom = null;

    // Kafka
    private final KafkaWriter newChannelWriter = new KafkaWriterImpl();
    private KafkaConsumer<String, String> channelConsumer;
    private KafkaConsumer<String, String> messageConsumer;

    // Json
    private final JsonActionParser actionMapper = new JsonActionParserImpl();
    private final JsonMessageParser messageMapper = new JsonMessageParserImpl();

    // Threads
    private final ExecutorService newChannelsCheck = Executors.newSingleThreadExecutor();
    private ExecutorService newMessageCheck;
    private volatile boolean isRunningMessages = false;
    private volatile boolean isRunningChannels = false;

    // cipher
    private ICipherService cipherService = new CipherService();
    private BigInteger currentWord = null;
    // endregion

    public LobbyView() {
        setSizeFull();
        setSpacing(false);
        // Header
        header = new H3();
        header.setWidthFull();

        // Channels Mod
        HorizontalLayout channelModLayout = getRoomsLayout();

        // Channels general layout
        VerticalLayout channelsLayout = new VerticalLayout();
        channelsLayout.setWidth("30%");
        channelsGrid = new Grid<>(RoomsInfo.class, false);
        channelsGrid.setHeightFull();
        channelsGrid.addColumn(RoomsInfo::getTitleLeft).setHeader("Channel Name");
        channelsGrid.addColumn(new ComponentRenderer<>(room -> {
            if (room != null) {
                long otherUser = id == room.getLeftUser() ? room.getRightUser() : room.getLeftUser();
                return new Span(userRegistrationService.getUserInfoById(otherUser).getUserName());
            }
            return new Span("");
        })).setHeader("Buddy Name");
        // chat layout
        VerticalLayout chatAndButtonPlace = new VerticalLayout();
        chatAndButtonPlace.setWidth("80%");
        chatPlace = new VerticalLayout();
        chatPlace.addClassName("scrollable-layout");

        chatPlace.setHeightFull();
        channelsGrid.addColumn(new ComponentRenderer<>(person -> {
            Button deleteButton = new Button("Delete chat");
            deleteButton.setTooltipText("Delete channel for all users");
            deleteButton.addClickListener(click -> {
                // Логика удаления
                if (person != null) {
                    if (currentRoom != null && person.getId() == currentRoom.getId()) {
                        chatPlace.removeAll();
                        isPersonDisconnected = true;
                        closeThreadMessages();
                        currentRoom = null;
                    }
                    cipherManageService.deleteCipherInfo(person.getCipherInfoId());
                    messagesMenageService.deleteAllMessagesByChatId(person.getId());
                    roomsManageService.deleteRoom(person.getId());

                    long otherUser = id == person.getLeftUser() ? person.getRightUser() : person.getLeftUser();
                    JsonAction action = JsonAction.builder().senderId(id).status("delete-channel").chatId(person.getId()).chatName(person.getTitleLeft()).build();
                    try {
                        newChannelWriter.processMessage(actionMapper.processStringAction(action), String.format("chatlistner.%s", otherUser));
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                    }
                    refreshChannels();
                } else {
                    openErrorNotification("Something went wrong please try again");
                }
            });
            return deleteButton;
        })).setHeader("Action");
        channelsGrid.addSelectionListener(selection -> {
            Optional<RoomsInfo> optionalPerson = selection.getFirstSelectedItem();
            optionalPerson.ifPresent(channel -> {
                isPersonDisconnected = true;
                if(currentRoom != null){
                    closeThreadMessages();
                    JsonMessage disconn = JsonMessage.builder().senderId(id).chatId(currentRoom.getId()).messageType("disconnected").build();
                    try {
                        newChannelWriter.processMessage(messageMapper.processJsonMessage(disconn), String.format("chatMessages-%s", currentRoom.getId()));
                        log.info("Message sent {}: {}", userName, disconn);
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                    }
                }
                currentRoom = channel;
                currentWord = DiffieHellman.generateRandomWord();
                BigInteger g = new BigInteger(currentRoom.getG());
                BigInteger p = new BigInteger(currentRoom.getP());
                BigInteger secretParam = g.modPow(currentWord, p);
                JsonMessage message = JsonMessage.builder().messageType("connected").senderId(id).chatId(currentRoom.getId()).keyWord(secretParam.toByteArray()).build();
                try {
                    newChannelWriter.processMessage(messageMapper.processJsonMessage(message), String.format("chatMessages-%s", currentRoom.getId()));
                    connectedMessageSent = true;
                    createThreadInitializeConsumer();
                    processMessages();
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                }
                refreshMessages();
            });
        });
        channelsLayout.add(header, channelModLayout, channelsGrid);
        channelsLayout.expand(channelsGrid);
        channelsLayout.expand(channelModLayout);
        // buttons and fields
        HorizontalLayout fieldsButtonsLayout = getChatPlaceLayout();
        chatAndButtonPlace.add(chatPlace, fieldsButtonsLayout);
        chatAndButtonPlace.expand(chatPlace);
        add(channelsLayout, chatAndButtonPlace);

    }

    // region UI and functions
    private HorizontalLayout getRoomsLayout() {
        HorizontalLayout channelModLayout = new HorizontalLayout();
        channelModLayout.setWidthFull();
        channelModLayout.setAlignItems(Alignment.END);
        TextField name = new TextField("Channel Name");
        Button addChanel = new Button(new Icon(VaadinIcon.PLUS), buttonClickEvent -> validateChannelInfo(name.getValue()));
        addChanel.setTooltipText("Add new channel");
        Button refreshChannels = new Button(new Icon(VaadinIcon.REFRESH), buttonClickEvent -> refreshChannels());
        refreshChannels.setTooltipText("Refresh channels list");
        channelModLayout.add(name, addChanel, refreshChannels);
        channelModLayout.expand(name);
        return channelModLayout;
    }

    private HorizontalLayout getChatPlaceLayout() {
        TextField message = new TextField("Your Message");
        Button sendButton = new Button(new Icon(VaadinIcon.PAPERPLANE));
        sendButton.setTooltipText("Send message");
        Button fileButton = new Button(new Icon(VaadinIcon.FILE));
        fileButton.setTooltipText("Send file");
        Button leaveChannelButton = new Button(new Icon(VaadinIcon.EXIT));
        leaveChannelButton.setTooltipText("Leave channel");
        sendButton.setDisableOnClick(true);
        fileButton.setDisableOnClick(true);
        sendButton.addClickListener(action -> {
            sendButton.setEnabled(true);
            if(message.getValue().isEmpty()){
                openErrorNotification("Message field can not be empty!");
                return;
            }
            if (currentRoom == null) {
                openErrorNotification("Please select a room");
                return;
            }
            if (isPersonDisconnected){
                openErrorNotification("All users should be in chat to exchange messages!");
                return;
            }
            try {
                sendMessage(message.getValue());
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }
            message.clear();
        });

        fileButton.addClickListener(action -> {
            fileButton.setEnabled(true);
            if (currentRoom == null) {
                openErrorNotification("Please select a room");
                return;
            }
            if (isPersonDisconnected){
                openErrorNotification("All users should be in chat to exchange messages!");
                return;
            }
            MemoryBuffer buffer = new MemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setMaxFileSize(104857600);
            upload.setMaxFiles(1);
            Dialog dialog = new Dialog();
            dialog.add(new H3("Upload your file"), upload);
            dialog.open();
            upload.addSucceededListener(succeededEvent -> {
                String fileName = succeededEvent.getFileName();
                String mimeType = succeededEvent.getMIMEType();
                String fileType = mimeType.split("/")[1];
                try {
                    byte[] bytes = toByteArray(buffer.getInputStream());
                    if (fileType.equals("png") || fileType.equals("jpg") || fileType.equals("jpeg")) {
                        sendImage(bytes);
                        dialog.close();
                    } else {
                        sendFile(bytes, fileName);
                        dialog.close();
                    }

                } catch (IOException e) {
                    log.error(e.getMessage());
                    openErrorNotification("Something went wrong please try again");
                }
            });
            upload.addFileRejectedListener(fileRejectedEvent -> openErrorNotification("File is too big, max size is 1MB"));
            fileButton.setEnabled(true);

        });

        leaveChannelButton.addClickListener(action -> {
            if(currentRoom == null){
                openErrorNotification("You dont have a room to leave!");
                return;
            }
            JsonMessage messageJson = JsonMessage.builder().senderId(id).messageType("disconnected").chatId(currentRoom.getId()).build();
            try {
                newChannelWriter.processMessage(messageMapper.processJsonMessage(messageJson), String.format("chatMessages-%d", currentRoom.getId()));
            } catch (JsonProcessingException e) {
                log.error(e.getMessage());
            }
            isPersonDisconnected = true;
            connectedMessageSent = false;
            currentRoom = null;
            closeThreadMessages();
            chatPlace.removeAll();
            Notification.show("Leaving channel",3000, Notification.Position.BOTTOM_END);
        });

        HorizontalLayout fieldsButtonsLayout = new HorizontalLayout();
        fieldsButtonsLayout.setWidthFull();
        fieldsButtonsLayout.setAlignItems(Alignment.END);
        fieldsButtonsLayout.add(leaveChannelButton, message, fileButton, sendButton);
        fieldsButtonsLayout.expand(message);
        return fieldsButtonsLayout;
    }

    private void validateChannelInfo(String channel) {
        if (channel == null || channel.isEmpty()) {
            openErrorNotification("Channel name should not be empty");
        } else {
            addChannel(channel);

        }
    }

    private void addChannel(String channel) {
        Dialog dialog = new Dialog();

        dialog.setHeaderTitle("New chat settings");
        TextField otherUserName = new TextField("Your buddy name");
        otherUserName.setRequiredIndicatorVisible(true);
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setAlignItems(Alignment.BASELINE);

        ComboBox<String> padding = new ComboBox<>("Padding", "ISO10126", "PKCS7", "Zeros", "ANSIX923");
        padding.setRequiredIndicatorVisible(true);

        ComboBox<String> cipherMode = new ComboBox<>("Cipher Mode", "CBC", "CFB", "OFB", "CTR", "ECB", "PCBC", "Random Delta");
        cipherMode.setRequiredIndicatorVisible(true);

        ComboBox<String> cipher = new ComboBox<>("Cipher Algorithm", "RC5", "MACGUFFIN");
        cipher.setRequiredIndicatorVisible(true);

        dialogLayout.add(otherUserName, padding, cipherMode, cipher);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidthFull();
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(click -> dialog.close());
        Button okButton = new Button("Apply settings");
        okButton.addClickListener(click -> {

            String chatPadding = padding.getValue();
            String chatMode = cipherMode.getValue();
            String chatCipher = cipher.getValue();
            String buddyName = otherUserName.getValue();
            if (chatMode == null || buddyName == null || chatCipher == null || chatPadding == null) {
                openErrorNotification("All fields must be filled!");
            } else {
                if(buddyName.equals(userName)){
                    openErrorNotification("You cant message yourself!");
                    return;
                }
                if (userRegistrationService.checkUserByUSerName(buddyName)) {
                    UserInfo buddy = userRegistrationService.getUserInfoByUsername(buddyName);
                    byte[] iv = DiffieHellman.getIV(8);
                    CipherInfo info = CipherInfo.builder().mode(chatMode).algorithm(chatCipher).padding(chatPadding)
                            .blockSizeBits(64).keySizeBits(128).iv(iv).build();
                    CipherInfo newInfo = cipherManageService.saveCipherInfo(info);
                    BigInteger[] pg = DiffieHellman.generateDiffieHellman(128);
                    byte[] p = pg[0].toByteArray();
                    byte[] g = pg[1].toByteArray();
                    JsonAction action = JsonAction.builder().chatName(channel).cipher(newInfo).status("new-channel").senderId(id).g(g).p(p).build();
                    try {
                        newChannelWriter.processMessage(actionMapper.processStringAction(action), String.format("chatlistner.%s", buddy.getId()));
                    } catch (JsonProcessingException e) {
                        log.error(e.getMessage());
                    }
                    dialog.close();
                } else {
                    openErrorNotification("There is no such buddy name!");
                }
            }


        });
        buttonsLayout.add(cancelButton, okButton);
        dialog.add(dialogLayout, buttonsLayout);
        dialog.open();


    }

    private void refreshChannels() {
        List<RoomsInfo> currentRooms = roomsManageService.getRoomsInfoForUser(id);
        channelsGrid.setItems(currentRooms);

    }

    private void refreshMessages() {
        chatPlace.removeAll();
        List<MessagesInfo> messagesInfos = messagesMenageService.getMessagesByChatId(currentRoom.getId());
        UserInfo buddy = null;
        for (MessagesInfo messagesInfo : messagesInfos) {
            if (messagesInfo.getSenderId() != id) {
                if (buddy == null) {
                    buddy = userRegistrationService.getUserInfoById(messagesInfo.getSenderId());
                }
                addMessage(messagesInfo, buddy.getUserName());
            } else {
                addMessage(messagesInfo, userName);
            }
        }
        printChatIds();
    }

    private void addMessage(MessagesInfo messagesInfo, String sender) {
        chatPlace.add(createMessage(messagesInfo, sender));
    }

    private void sendMessage(String message) throws JsonProcessingException {
        MessagesInfo newMessage = MessagesInfo.builder()
                .messageType("TEXT").message(message.getBytes(StandardCharsets.UTF_8))
                .senderId(id).chatId(currentRoom.getId()).componentId(0).timestamp(LocalDateTime.now()).build();
        newMessage = messagesMenageService.saveMessage(newMessage);
        JsonMessage kafkaMsg = makeJsonMessage(newMessage);
        kafkaMsg.setMessage(cipherService.encrypt(kafkaMsg.getMessage()));
        addMessage(newMessage, userName);
        newChannelWriter.processMessage(messageMapper.processJsonMessage(kafkaMsg), String.format("chatMessages-%s", newMessage.getChatId()));

    }

    private void sendImage(byte[] imageBytes) throws JsonProcessingException {
        MessagesInfo newMessage = MessagesInfo.builder()
                .message(imageBytes).messageType("IMAGE").timestamp(LocalDateTime.now())
                .componentId(0).senderId(id).chatId(currentRoom.getId()).build();
        newMessage = messagesMenageService.saveMessage(newMessage);
        JsonMessage kafkaMsg = makeJsonMessage(newMessage);
        kafkaMsg.setMessage(cipherService.encrypt(kafkaMsg.getMessage()));
        addMessage(newMessage, userName);
        newChannelWriter.processMessage(messageMapper.processJsonMessage(kafkaMsg), String.format("chatMessages-%s", newMessage.getChatId()));
    }

    private void sendFile(byte[] fileBytes, String fileName) throws JsonProcessingException {
        MessagesInfo fileMessage = MessagesInfo.builder()
                .message(fileBytes).messageType(fileName).chatId(currentRoom.getId())
                .senderId(id).componentId(0).timestamp(LocalDateTime.now()).build();
        fileMessage = messagesMenageService.saveMessage(fileMessage);
        JsonMessage kafkaMsg = makeJsonMessage(fileMessage);
        kafkaMsg.setMessage(cipherService.encrypt(kafkaMsg.getMessage()));
        addMessage(fileMessage, userName);
        newChannelWriter.processMessage(messageMapper.processJsonMessage(kafkaMsg), String.format("chatMessages-%s", fileMessage.getChatId()));
    }

    private void printChatIds(){
        chatPlace.getChildren().forEach(child -> child.getId().ifPresent(log::info));
    }

    private Div createMessage(MessagesInfo info, String sender) {
        // div holder
        Div messageDiv = new Div();
        messageDiv.addClassName("message-container");
        String formattedDateTime = info.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Span span = new Span(String.format("%s [%s]", sender, formattedDateTime));
        span.addClassName("sender-name");
        switch (info.getMessageType()) {
            case "TEXT":
                String message = new String(info.getMessage(), StandardCharsets.UTF_8);
                Paragraph paragraph = new Paragraph(message);
                paragraph.addClassName("message-text");
                messageDiv.add(span, paragraph);
                break;
            case "IMAGE":
                StreamResource resource = new StreamResource("image", () -> new ByteArrayInputStream(info.getMessage()));
                Image image = new Image(resource, "Generated Image");
                image.addClassName("message-image");
                messageDiv.add(span, image);
                break;
            default:
                StreamResource resourceFile = new StreamResource(info.getMessageType(), () -> new ByteArrayInputStream(info.getMessage()));
                Anchor anchor = new Anchor(resourceFile, info.getMessageType());
                anchor.getElement().setAttribute("download", true);
                anchor.setText("Download file: " + info.getMessageType());
                anchor.addClassName("message-link");
                messageDiv.add(span, anchor);
                break;
        }
        messageDiv.setId(String.format("%d", info.getId()));
        log.info("create message {}", messageDiv.getId());
        // Context menu creation
        ContextMenu menu = new ContextMenu(span);
        menu.getClassNames().add("custom-context-menu");
        menu.setOpenOnClick(true);
        menu.addItem("Delete", menuItemClickEvent -> {
            Optional<Component> parent = span.getParent();
            if (parent.isPresent() && parent.get() instanceof Div) {
                chatPlace.remove(parent.get()); // Удаляем Div из основного контейнера
                long messageId = Long.parseLong(messageDiv.getId().orElse("0"));
                JsonMessage deleteMessage = JsonMessage.builder().id(messageId).senderId(id).messageType("delete").build();
                try {
                    newChannelWriter.processMessage(messageMapper.processJsonMessage(deleteMessage), "chatMessages-" + currentRoom.getId());
                } catch (JsonProcessingException e) {
                    log.error(e.getMessage());
                }
                messagesMenageService.deleteMessageById(info.getId(), currentRoom.getId()); // Логика удаления файла на сервере
            }
        });
        return messageDiv;
    }

    // endregion
    // region utils
    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        id = Long.parseLong(beforeEnterEvent.getRouteParameters().get("id").orElse("-1"));
        userName = beforeEnterEvent.getRouteParameters().get("name").orElse("");
        UserInfo registered = userRegistrationService.getUserInfoById(id);
        if(id == -1 || userName.isEmpty() || registered == null || !registered.getUserName().equals(userName)) {
            beforeEnterEvent.rerouteTo(ErrorView.class);
        } else {
            header.setText(String.format("Welcome to chat, %s!", userName));
            createTopic(String.format("chatlistner.%s", id), 1, (short) 1);
            initializeChannelConsumer();
            processChatRequests();
            refreshChannels();
        }
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        return buffer.toByteArray();
    }

    // endregion
    // region kafka
    private KafkaConsumer<String, String> initializeConsumer(String groupId, String topic) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "104857600");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(topic));
        return consumer;
    }

    private void initializeChannelConsumer() {
        channelConsumer = initializeConsumer("channel reader", String.format("chatlistner.%s", id));

    }

    private void initializeMessageConsumer() {
        messageConsumer = initializeConsumer(String.format("chat-%s", id), String.format("chatMessages-%s", currentRoom.getId()));
    }

    public static void createTopic(String topicName, int numPartitions, short replicationFactor) {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

        try (AdminClient adminClient = AdminClient.create(config)) {
            // Проверяем, существует ли топик
            if (!adminClient.listTopics().names().get().contains(topicName)) {
                // Создаем новый топик
                NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor);
                adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
                System.out.println("Топик создан: " + topicName);
            } else {
                System.out.println("Топик уже существует: " + topicName);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void processChatRequests() {
        newChannelsCheck.submit(() -> {
            try {
                JsonActionParser mapper = new JsonActionParserImpl();
                isRunningChannels = true;
                while (isRunningChannels) {
                    ConsumerRecords<String, String> records = channelConsumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        String action = record.value();
                        if (!action.startsWith("{")) {
                            continue;
                        }
                        JsonAction currentAction = mapper.processJsonAction(action);

                        System.out.println(currentAction);
                        if (currentAction.getSenderId() != id) {
                            if (currentAction.getStatus().equals("new-channel") && currentAction.getP() != null) {

                                long senderId = currentAction.getSenderId();

                                RoomsInfo newRoom = roomsManageService.saveRoom(RoomsInfo.builder()
                                        .p(currentAction.getP()).g(currentAction.getG())
                                        .titleRight(currentAction.getChatName()).titleLeft(currentAction.getChatName())
                                        .rightUser(id).leftUser(senderId)
                                        .cipherInfoId(currentAction.getCipher().getId()).build());
                                createTopic(String.format("chatMessages-%s", newRoom.getId()), 1, (short) 1);
                                currentUIChannel.access(() -> {
                                    refreshChannels();
                                    Dialog dial = new Dialog();
                                    dial.add(new Span("Received action: " + currentAction));
                                    dial.open();
                                });
                                currentAction.setSenderId(id);
                                currentAction.setStatus("accepted");
                                newChannelWriter.processMessage(mapper.processStringAction(currentAction), String.format("chatlistner.%s", senderId));

                            } else if (currentAction.getStatus().equals("accepted")) {
                                currentUIChannel.access(() -> {
                                    refreshChannels();
                                    Dialog dial = new Dialog();
                                    dial.add(new Span("Received action: " + currentAction));
                                    dial.open();
                                });
                            } else if (currentAction.getStatus().equals("delete-channel")) {
                                currentUIChannel.access(() -> {
                                    if (currentRoom != null && currentRoom.getId() == currentAction.getChatId()){
                                        currentRoom = null;
                                        isPersonDisconnected = true;
                                        closeThreadMessages();
                                        chatPlace.removeAll();
                                        refreshChannels();
                                        Notification.show("Current chat has been deleted by other user", 3000, Notification.Position.BOTTOM_END);
                                    }
                                    chatPlace.removeAll();
                                    refreshChannels();
                                    Dialog dial = new Dialog();
                                    dial.add(new Span("User " + userRegistrationService.getUserInfoById(currentAction.getSenderId()).getUserName() + " has deleted chat: " + currentAction.getChatName()));
                                    dial.open();
                                });
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally{
                if(channelConsumer != null) {
                    channelConsumer.close();
                    channelConsumer = null;
                }
            }
        });
    }

    private void processMessages() {
        newMessageCheck.submit(() -> {
            try {
                JsonMessageParser mapper = new JsonMessageParserImpl();
                isRunningMessages = true;
                ICipherService service = new CipherService();
                while (isRunningMessages) {
                    ConsumerRecords<String, String> records = messageConsumer.poll(100);
                    for (ConsumerRecord<String, String> record : records) {
                        String action = record.value();
                        if (!action.startsWith("{")) {
                            continue;
                        }
                        JsonMessage message = mapper.processStringMessage(action);
                        log.info("Recieved message for user {}: {}", userName, message);
                        if (message.getSenderId() != id) {
                            switch (message.getMessageType()){
                                case "disconnected":
                                    if (currentUIChannel != null) {
                                        currentUIMessages.access(() -> {
                                            isPersonDisconnected = true;
                                            connectedMessageSent = false;
                                        });
                                    }

                                    break;
                                case "connected":
                                    if (currentUIChannel != null){
                                        if (!connectedMessageSent){
                                            currentWord = DiffieHellman.generateRandomWord();
                                            BigInteger g = new BigInteger(currentRoom.getG());
                                            BigInteger p = new BigInteger(currentRoom.getP());
                                            BigInteger secretParam = g.modPow(currentWord, p);
                                            JsonMessage mess = JsonMessage.builder().messageType("connected").senderId(id).chatId(currentRoom.getId()).keyWord(secretParam.toByteArray()).build();
                                            try {
                                                newChannelWriter.processMessage(messageMapper.processJsonMessage(mess), String.format("chatMessages-%s", currentRoom.getId()));
                                                connectedMessageSent = true;
                                            } catch (Exception e) {
                                                log.error(e.getMessage(), e);
                                            }
                                        }
                                        BigInteger otherKeyWord = new BigInteger(message.getKeyWord());
                                        BigInteger p = new BigInteger(currentRoom.getP());
                                        byte[] key = otherKeyWord.modPow(currentWord, p).toByteArray();
                                        log.info("Created key {}", new BigInteger(key));
                                        CipherInfo cipherInfo = cipherManageService.getCipherInfo(currentRoom.getCipherInfoId());
                                        service.setCipherSettings(cipherInfo.getPadding(), cipherInfo.getMode(),
                                                cipherInfo.getAlgorithm(), cipherInfo.getBlockSizeBits(), cipherInfo.getKeySizeBits(), 32, cipherInfo.getIv());
                                        service.setCipherKey(key);
                                        currentUIMessages.access(() -> {
                                                    isPersonDisconnected = false;
                                                    cipherService.setCipherSettings(cipherInfo.getPadding(), cipherInfo.getMode(),
                                                            cipherInfo.getAlgorithm(), cipherInfo.getBlockSizeBits(), cipherInfo.getKeySizeBits(), 32, cipherInfo.getIv());
                                                    cipherService.setCipherKey(key);
                                                }
                                        );
                                    }
                                    break;
                                case "delete":
                                    if (currentUIChannel != null){
                                        currentUIMessages.access(() -> {
                                            log.info("deletinf messege: {}", message.getId());
                                            Optional<Component> divToDelete = chatPlace.getChildren().filter(child -> String.format("%d", message.getId()).equals(child.getId().orElse(null))).findFirst();
                                            chatPlace.getChildren().forEach(child -> child.getId().ifPresent(log::info));
                                            divToDelete.ifPresent(chatPlace::remove);
                                        });
                                    }
                                    break;
                                default:
                                    if (currentUIChannel != null){
                                        message.setMessage(service.decrypt(message.getMessage()));
                                        MessagesInfo msg = getMessageFromJsonMessage(message);
                                        currentUIMessages.access(() -> addMessage(msg, userRegistrationService.getUserInfoById(msg.getSenderId()).getUserName()));
                                    }
                                    break;

                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            } finally {
                if(messageConsumer != null){
                    messageConsumer.close();
                    messageConsumer = null;
                }
            }
        });
    }


    private void closeThreadMessages(){
        isRunningMessages = false;
        newMessageCheck.shutdown(); // Остановить ExecutorService
        try {
            if (!newMessageCheck.awaitTermination(5, TimeUnit.SECONDS)) {
                newMessageCheck.shutdownNow();  // Принудительная остановка, если поток не завершился
            }
        } catch (InterruptedException e) {
            newMessageCheck.shutdownNow();
            Thread.currentThread().interrupt();  // Восстановить статус прерывания
        }
    }

    private void closeThreadChannels(){
        isRunningChannels = false;
        newChannelsCheck.shutdown(); // Остановить ExecutorService
        try {
            if (!newChannelsCheck.awaitTermination(5, TimeUnit.SECONDS)) {
                newChannelsCheck.shutdownNow();  // Принудительная остановка, если поток не завершился
            }
        } catch (InterruptedException e) {
            newChannelsCheck.shutdownNow();
            Thread.currentThread().interrupt();  // Восстановить статус прерывания
        }

    }

    private void createThreadInitializeConsumer(){
        newMessageCheck = Executors.newSingleThreadExecutor();
        initializeMessageConsumer();
    }
    // endregion

    private JsonMessage makeJsonMessage(MessagesInfo messagesInfo){
        return JsonMessage.builder()
                .message(messagesInfo.getMessage()).id(messagesInfo.getId()).messageType(messagesInfo.getMessageType())
                .timestamp(messagesInfo.getTimestamp()).chatId(messagesInfo.getChatId()).senderId(messagesInfo.getSenderId())
                .build();
    }

    private MessagesInfo getMessageFromJsonMessage(JsonMessage message){
        return MessagesInfo.builder()
                .message(message.getMessage()).messageType(message.getMessageType()).timestamp(message.getTimestamp())
                .senderId(message.getSenderId()).chatId(message.getId()).componentId(0).build();
    }

//    @PreDestroy
//    private void destroyer(){
//        if (newMessageCheck != null) {
//            closeThreadMessages();
//        }
//        closeThreadChannels();
//    }

    @Override
    public void beforeLeave(BeforeLeaveEvent beforeLeaveEvent) {
        JsonMessage message = JsonMessage.builder().messageType("disconnect").id(id).chatId(currentRoom.getId()).build();
        try {
            newChannelWriter.processMessage(messageMapper.processJsonMessage(message), String.format("chatListner-%d", currentRoom.getId()));
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }finally {
            if (newMessageCheck != null) {
                closeThreadMessages();
            }
            closeThreadChannels();
            newChannelWriter.close();
        }

    }
}
