package com.telecomsockets.views;

import com.telecomsockets.models.ChatMessageModel;
import com.telecomsockets.models.ChatUser;
import com.telecomsockets.views.MessageInputBox.SendHandler;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ChatView extends BorderPane {
    private MessageList messageList;
    private MessageInputBox messageInputBox;
    private ListView<ChatUser> list;

    public ChatView(ChatUser me) {
        super();

        leftProperty().set(createLeft());
        centerProperty().bind(createCenter(me));
    }

    public void setItems(ObservableList<ChatUser> items) {
        list.setItems(items);
    }

    public ReadOnlyObjectProperty<ChatUser> selectedItemProperty() {
        return list.getSelectionModel().selectedItemProperty();
    }

    private Region createLeft() {
        list = new ChatUsersListView();
        BorderPane.setMargin(list, new Insets(10, 5, 10, 10));
        return list;
    }


    private ObservableValue<Node> createCenter(ChatUser me) {
        return Bindings.when(selectedItemProperty().isNull())
                .<Node>then(new Label("Seleccione un chat para ver sus mensajes")).otherwise(createChat(me))
                .map(node -> {
                    BorderPane.setMargin(node, new Insets(10, 10, 10, 5));
                    return node;
                });

    }

    private Node createChat(ChatUser me) {
        var myId = me.id();
        var myName = me.name();
        var title = createTitle(myName);
        messageList = new MessageList(myId);
        messageInputBox = new MessageInputBox();

        var center = new VBox();
        VBox.setVgrow(messageList, Priority.ALWAYS);
        VBox.setMargin(messageInputBox, new Insets(10, 0, 0, 0));
        center.getChildren().addAll(title, messageList, messageInputBox);

        return center;
    }

    public ObjectProperty<ObservableList<ChatMessageModel>> messagesProperty() {
        return messageList.itemsProperty();
    }

    public void setOnSendMessage(SendHandler onSendMessage) {
        messageInputBox.setOnSendMessage(onSendMessage);
    }

    public MessageInputBox getInputField() {
        return messageInputBox;
    }


    private Node createTitle(String myName) {
        var container = new VBox();
        var titleLabel = new Label();
        titleLabel.getStyleClass().add("chat-title-tag");
        titleLabel.textProperty().bind(selectedItemProperty().map(ChatUser::name).orElse("..."));
        container.getChildren().add(titleLabel);
        return container;
    }


}
