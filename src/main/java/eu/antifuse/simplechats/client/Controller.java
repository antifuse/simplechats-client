package eu.antifuse.simplechats.client;

import eu.antifuse.simplechats.Transmission;
import eu.antifuse.simplechats.client.Client;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.net.InetSocketAddress;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller extends Application {

    private Client client;
    private TrayIcon trayIcon;
    private SystemTray tray;

    @FXML
    private TextArea outArea;

    @FXML
    private TextField address;

    @FXML
    private TextField message;

    @FXML
    private TextField name;

    @FXML
    private void handleSend(javafx.scene.input.MouseEvent event) {
        this.sendMessage();
    }

    @FXML
    private void handleEnter(javafx.scene.input.KeyEvent keyEvent) {
        if (keyEvent.getCode().equals(KeyCode.ENTER)) {
            this.sendMessage();
        }
    }

    @FXML
    private void handleConnect(javafx.scene.input.MouseEvent event) {
        if (client != null) client.disconnect();
        client = new Client();
        String addrStr = address.getText();
        String ip;
        int port;
        if (addrStr.split(":").length > 1) {
            ip = addrStr.split(":")[0];
            port = Integer.parseInt(addrStr.split(":")[1]);
        } else {
            ip = addrStr;
            port = 2311;
        }
        client.setUsername(name.getText());
        client.setGui(this);
        client.connect(new InetSocketAddress(ip, port));
    }

    private void sendMessage() {
        client.sendMessage(message.getText());
        message.clear();
    }

    public void processMessage(Transmission message) throws AWTException {
        if (this.trayIcon == null) {
            this.tray = SystemTray.getSystemTray();
            this.trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().createImage(Thread.currentThread().getContextClassLoader().getResource("chat.png")), "SimpleChats");
            this.trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        }
        switch(message.getType()) {
            case JOIN: {
                this.outArea.appendText("\n"+ message.data(0) + " joined");
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage(message.data(0) + " joined.", "", MessageType.INFO);
                break;
            }
            case LEAVE: {
                this.outArea.appendText("\n"+ message.data(0) + " left.");
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage(message.data(0) + " left.", "", MessageType.INFO);
                break;
            }
            case USERLIST: {
                this.outArea.appendText("\n"+ message.size() + " people are online:");
                for (String name: message.getData()) {
                    this.outArea.appendText("\n"+ name);
                }
                break;
            }
            case NAMECHANGE: {
                this.outArea.appendText("\n" + message.data(0) + " changed their name to " + message.data(1));
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage(message.data(0) + " changed their name to " + message.data(1), "", MessageType.INFO);
                break;

            }
            case MESSAGE: {
                this.outArea.appendText("\n[" + message.data(0) + "] " + message.data(1));
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage(message.data(0), message.data(1), MessageType.NONE);
                break;
            }
            case SYSTEM: {
                this.outArea.appendText("\n" + message.data(0));
                break;
            }
        }

        /*
        if (m.find()) {
            trayIcon.displayMessage(m.group(1),m.group(2), MessageType.NONE);
        } else {
            trayIcon.displayMessage("SimpleChats", message, MessageType.INFO);
        }*/

    }

    FXMLLoader loader;
    @Override
    public void start(Stage primaryStage) throws Exception {
        loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("clientgui.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("SimpleChats Client");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        TextArea out = (TextArea) loader.getNamespace().get("outArea");
        out.textProperty().addListener((ChangeListener<String>) (ObservableValue<? extends String> observable, String old, String n)->{
            out.setScrollTop(Double.MAX_VALUE);
        });
    }

    @Override
    public void stop() throws Exception {
        if (this.client != null) this.client.disconnect();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
