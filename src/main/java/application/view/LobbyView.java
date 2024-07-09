package application.view;

import application.view.extenders.NotificationHolder;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.virtuallist.VirtualList;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Route("lobby")
@PageTitle("Lobby")
@CssImport("./styles/styles.css")
public class LobbyView extends HorizontalLayout implements HasUrlParameter<Long>, NotificationHolder {
    private long id;
    private final VirtualList<String> channelsList;
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
        Button addChanel = new Button("Add Channel", buttonClickEvent -> validateChannelInfo(name.getValue()));
        addChanel.setDisableOnClick(true);
        channelModLayout.add(name, addChanel);
        channelModLayout.expand(name);

        // Channels general layout
        VerticalLayout channelsLayout = new VerticalLayout();
        channelsLayout.setWidth("50%");
        channelsList = new VirtualList<>();
        channelsList.setHeightFull();
        channelsLayout.add(header, channelModLayout, channelsList);
        channelsLayout.expand(channelsList);
        channelsLayout.expand(channelModLayout);

        // chat layout
        VerticalLayout chatAndButtonPlace = new VerticalLayout();
        chatAndButtonPlace.setWidth("50%");
        chatPlace = new VerticalLayout();
        chatPlace.addClassName("scrollable-layout");

        chatPlace.setHeightFull();
        // buttons and fields
        TextField message = new TextField("Your Message");
        Button sendButton = new Button(new Icon(VaadinIcon.PAPERPLANE));
        Button fileButton = new Button(new Icon(VaadinIcon.FILE));
        sendButton.setDisableOnClick(true);
        fileButton.setDisableOnClick(true);
        sendButton.addClickListener(action -> {
            sendMessage(message.getValue());
            sendButton.setEnabled(true);
            message.clear();
        });


        HorizontalLayout fieldsButtonsLayout = new HorizontalLayout();
        fieldsButtonsLayout.setWidthFull();
        fieldsButtonsLayout.setAlignItems(Alignment.END);
        fieldsButtonsLayout.add(message, fileButton, sendButton);
        fieldsButtonsLayout.expand(message);
        chatAndButtonPlace.add(chatPlace, fieldsButtonsLayout);
        chatAndButtonPlace.expand(chatPlace);


        channelsList.setItems("Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho", "Hi", "zoro", "Luffy", "duccy", "poncho");
        add(channelsLayout, chatAndButtonPlace);

    }

    private void validateChannelInfo(String channel) {
        if (channel == null || channel.isEmpty()) {
            openErrorNotification("Channel name should not be empty");
        }
    }

    private void addChannel(String channel) {

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
        menu.addItem("Delete for me");
        menu.addItem("Delete for all");
        return messageDiv;
    }
}
