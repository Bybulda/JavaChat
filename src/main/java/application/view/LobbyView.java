package application.view;

import application.model.Channel;
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
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Route("lobby")
@PageTitle("Lobby")
@CssImport("./styles/styles.css")
public class LobbyView extends HorizontalLayout implements HasUrlParameter<Long>, NotificationHolder {
    private long id;
    private final Grid<Channel> channelsGrid;
    private final VerticalLayout chatPlace;
    private final H3 header;
    private long messageId = 1;

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long aLong) {
        id = aLong;
        header.setText(String.format("Hello %d", id));
    }

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
        channelsGrid = new Grid<>(Channel.class, false);
        channelsGrid.setHeightFull();
        channelsGrid.addColumn(Channel::getChannelName).setHeader("Channel Name");
        channelsGrid.addColumn(Channel::getBuddy).setHeader("Buddy");
        channelsGrid.addColumn(new ComponentRenderer<>(person -> {
            Button deleteButton = new Button("Удалить");
            deleteButton.setTooltipText("Delete channel for all users");
            deleteButton.addClickListener(click -> {
                // Логика удаления
                channelsGrid.getDataProvider().refreshAll();
            });
            return deleteButton;
        })).setHeader("Action");
        channelsGrid.addSelectionListener(selection -> {
            Optional<Channel> optionalPerson = selection.getFirstSelectedItem();
            optionalPerson.ifPresent(channel -> Notification.show(channel.getChannelName(), 3000, Notification.Position.BOTTOM_END));
        });
        channelsGrid.setItems(List.of(new Channel(1, 1, 1,"name", "nigga")));
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
            sendMessage(message.getValue());
            sendButton.setEnabled(true);
            message.clear();
        });

        fileButton.addClickListener(action -> {
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
        VerticalLayout dialogLayout = new VerticalLayout();
        dialogLayout.setAlignItems(Alignment.BASELINE);

        ComboBox<String> padding = new ComboBox<>("Padding", "ISO10126", "PKCS7", "Zeros", "ANSIX923");
        padding.setRequiredIndicatorVisible(true);

        ComboBox<String> cipherMode = new ComboBox<>("Cipher Mode", "CBC", "CFB", "OFB", "CTR", "ECB", "PCBC", "Random Delta");
        cipherMode.setRequiredIndicatorVisible(true);

        ComboBox<String> cipher = new ComboBox<>("Cipher Algorithm", "RC5", "MACGUFFIN");
        cipherMode.setRequiredIndicatorVisible(true);

        dialogLayout.add(padding, cipherMode, cipher);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setWidthFull();
        Button cancelButton = new Button("Cancel");
        cancelButton.addClickListener(click -> {dialog.close();});
        Button okButton = new Button("Apply settings");
        okButton.addClickListener(click -> {});
        buttonsLayout.add(cancelButton, okButton);
        dialog.add(dialogLayout, buttonsLayout);
        dialog.open();



    }

    private void refreshChannels(){

    }

    private void sendMessage(String message) {
        chatPlace.add(createMessage(message));

    }

    private Div createMessage(String message) {
        // div holder
        Div messageDiv = new Div();
        messageDiv.addClassName("message-container");
        // Span
        LocalDateTime currentDateTime = LocalDateTime.now();
        String formattedDateTime = currentDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Span span = new Span(String.format("Alex [%s]", formattedDateTime));
        span.addClassName("sender-name");
        messageDiv.add(span);

        Paragraph paragraph = new Paragraph(message);
        paragraph.addClassName("message-text");
        messageDiv.add(paragraph);

        // Context menu creation
        ContextMenu menu = new ContextMenu(messageDiv);
        menu.getClassNames().add("custom-context-menu");
        menu.setOpenOnClick(true);
        menu.addItem("Delete");
        return messageDiv;
    }

    private void test(){

    }
}
