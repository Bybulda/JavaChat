package application.view;

import application.backend.model.CipherInfo;
import application.backend.model.MessagesInfo;
import application.backend.model.RoomsInfo;
import application.backend.model.UserInfo;
import application.backend.service.CipherManageService;
import application.backend.service.MessagesMenageService;
import application.backend.service.RoomsManageService;
import application.backend.service.UserRegistrationService;
import application.view.extenders.NotificationHolder;
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
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route("lobby/:id/:name")
@PageTitle("Lobby")
@CssImport("./styles/styles.css")
public class LobbyView extends HorizontalLayout implements BeforeEnterObserver, NotificationHolder {
    // Services
    @Autowired
    CipherManageService cipherManageService;
    @Autowired
    MessagesMenageService messagesMenageService;
    @Autowired
    RoomsManageService roomsManageService;
    @Autowired
    UserRegistrationService userRegistrationService;

    // user info
    private long id;
    private String userName;
    // ui
    private final Grid<RoomsInfo> channelsGrid;
    private final VerticalLayout chatPlace;
    private final H3 header;
    // chat info
    private RoomsInfo currentRoom = null;
    private long messageId = 1;


    public LobbyView() {
        setSizeFull();
        setSpacing(false);
        // Header
        header = new H3();
        header.setWidthFull();

        // Channels Mod
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

        // Channels general layout
        VerticalLayout channelsLayout = new VerticalLayout();
        channelsLayout.setWidth("30%");
        channelsGrid = new Grid<>(RoomsInfo.class, false);
        channelsGrid.setHeightFull();
        channelsGrid.addColumn(RoomsInfo::getTitleLeft).setHeader("Channel Name");
        channelsGrid.addColumn(new ComponentRenderer<>(person -> {
            Button deleteButton = new Button("Удалить");
            deleteButton.setTooltipText("Delete channel for all users");
            deleteButton.addClickListener(click -> {
                // Логика удаления
                if (person != null) {
                    cipherManageService.deleteCipherInfo(person.getCipherInfoId());
                    messagesMenageService.deleteAllMessagesByChatId(person.getId());
                    roomsManageService.deleteRoom(person.getId());
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
                currentRoom = channel;
                // TODO: kafka active message send
                refreshMessages();
            });
        });
        channelsLayout.add(header, channelModLayout, channelsGrid);
        channelsLayout.expand(channelsGrid);
        channelsLayout.expand(channelModLayout);

        // chat layout
        VerticalLayout chatAndButtonPlace = new VerticalLayout();
        chatAndButtonPlace.setWidth("80%");
        chatPlace = new VerticalLayout();
        chatPlace.addClassName("scrollable-layout");

        chatPlace.setHeightFull();
        // buttons and fields
        TextField message = new TextField("Your Message");
        Button sendButton = new Button(new Icon(VaadinIcon.PAPERPLANE));
        sendButton.setTooltipText("Send message");
        Button fileButton = new Button(new Icon(VaadinIcon.FILE));
        fileButton.setTooltipText("Send file");
        sendButton.setDisableOnClick(true);
        fileButton.setDisableOnClick(true);
        sendButton.addClickListener(action -> {
            if (currentRoom == null) {
                openErrorNotification("Please select a room");
                return;
            }
            sendMessage(message.getValue());
            sendButton.setEnabled(true);
            message.clear();
        });

        fileButton.addClickListener(action -> {
            if (currentRoom == null) {
                openErrorNotification("Please select a room");
                return;
            }
            MultiFileMemoryBuffer buffer = new MultiFileMemoryBuffer();
            Upload upload = new Upload(buffer);
            upload.setMaxFiles(1);
            upload.addSucceededListener(succeededEvent -> {
                String fileName = succeededEvent.getFileName();
                System.out.println(fileName);
            });
            Dialog dialog = new Dialog();
            dialog.add(new H3("Upload your file"), upload);
            dialog.open();

            fileButton.setEnabled(true);

        });


        HorizontalLayout fieldsButtonsLayout = new HorizontalLayout();
        fieldsButtonsLayout.setWidthFull();
        fieldsButtonsLayout.setAlignItems(Alignment.END);
        fieldsButtonsLayout.add(message, fileButton, sendButton);
        fieldsButtonsLayout.expand(message);
        chatAndButtonPlace.add(chatPlace, fieldsButtonsLayout);
        chatAndButtonPlace.expand(chatPlace);
        add(channelsLayout, chatAndButtonPlace);

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
                if (userRegistrationService.checkUserByUSerName(buddyName)) {
                    UserInfo buddy = userRegistrationService.getUserInfoByUsername(buddyName);
                    byte[] iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
                    CipherInfo info = CipherInfo.builder().mode(chatMode).algorithm(chatCipher).padding(chatPadding)
                            .blockSizeBits(128).keySizeBits(96).iv(iv).build();
                    CipherInfo newInfo = cipherManageService.saveCipherInfo(info);
                    RoomsInfo room = RoomsInfo.builder().
                            cipherInfoId(newInfo.getId())
                            .leftUser(id)
                            .rightUser(buddy.getId()).titleRight(channel).titleLeft(channel)
                            .g(new byte[]{}).p(new byte[]{}).build();
                    roomsManageService.saveRoom(room);
                    refreshChannels();
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
    }

    private void addMessage(MessagesInfo messagesInfo, String sender) {
        chatPlace.add(createMessage(messagesInfo, sender));
    }

    private void sendMessage(String message) {
        MessagesInfo newMessage = MessagesInfo.builder()
                .messageType("TEXT").message(message.getBytes(StandardCharsets.UTF_8))
                .senderId(id).chatId(currentRoom.getId()).componentId(0).timestamp(LocalDateTime.now()).build();
        messagesMenageService.saveMessage(newMessage);
        addMessage(newMessage, userName);

    }

    private Div createMessage(MessagesInfo info, String sender) {
        // div holder
        Div messageDiv = new Div();
        String formattedDateTime = info.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Span span = new Span(String.format("%s [%s]", sender, formattedDateTime));
        span.addClassName("sender-name");
        switch (info.getMessageType()) {
            case "TEXT":
                String message = new String(info.getMessage(), StandardCharsets.UTF_8);
                messageDiv.addClassName("message-container");
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
            case "FILE":
                break;
            default:
                break;
        }
        // Context menu creation
        ContextMenu menu = new ContextMenu(messageDiv);
        menu.getClassNames().add("custom-context-menu");
        menu.setOpenOnClick(true);
        menu.addItem("Delete");
        messageDiv.setId(String.format("%d", info.getId()));
        return messageDiv;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        id = Long.parseLong(beforeEnterEvent.getRouteParameters().get("id").orElse(""));
        userName = beforeEnterEvent.getRouteParameters().get("name").orElse("");
        header.setText(String.format("Welcome to chat, %s!", userName));
    }
}
