package eu.antifuse.simplechats.client;

import eu.antifuse.simplechats.Transmission;
import eu.antifuse.simplechats.client.Client;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import java.awt.*;
import java.awt.TrayIcon.MessageType;
import java.net.InetSocketAddress;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Controller extends Application implements Initializable {

    private Client client;
    private TrayIcon trayIcon;
    private SystemTray tray;
    private ResourceBundle strings;

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
        if (name.getText() == null || name.getText().equals("")) return;
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
                this.outArea.appendText("\n"+ getString("ms.join", message.data(0)));
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage(getString("ms.join", message.data(0)), "", MessageType.INFO);
                break;
            }
            case LEAVE: {
                this.outArea.appendText("\n"+ getString("ms.leave", message.data(0)));
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage(getString("ms.leave", message.data(0)), "", MessageType.INFO);
                break;
            }
            case USERLIST: {
                this.outArea.appendText("\n"+ getString("ms.onlineCount", message.size()));
                for (String name: message.getData()) {
                    this.outArea.appendText("\n"+ name);
                }
                break;
            }
            case NAMECHANGE: {
                this.outArea.appendText("\n" + getString("ms.nameChange", message.data(0), message.data(1)));
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage(getString("ms.nameChange", message.data(0), message.data(1)), "", MessageType.INFO);
                break;

            }
            case MESSAGE: {
                this.outArea.appendText("\n[" + message.data(0) + "] " + message.data(1));
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage(message.data(0), message.data(1), MessageType.NONE);
                break;
            }
            case DIRECT: {
                this.outArea.appendText("\n[" + message.data(0) + " >> " + this.client.getUsername() + "] " + message.data(1));
                if(!this.outArea.getScene().getWindow().isFocused()) trayIcon.displayMessage("[DM]" + message.data(0), message.data(1), MessageType.NONE);
                break;
            }
            case SYSTEM: {
                this.outArea.appendText("\n" + getString(message.data(0)));
                break;
            }
        }
    }

    FXMLLoader loader;
    @Override
    public void start(Stage primaryStage) throws Exception {
        loader = new FXMLLoader(Thread.currentThread().getContextClassLoader().getResource("clientgui.fxml"), ResourceBundle.getBundle("strings", Locale.getDefault()));
        Parent root = loader.load();
        primaryStage.setTitle("SimpleChats Client");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        TextArea out = (TextArea) loader.getNamespace().get("outArea");
        TextField address = (TextField) loader.getNamespace().get("address");
        TextField username = (TextField) loader.getNamespace().get("name");
        if (this.getParameters().getNamed().containsKey("defaultIP")) address.setText(this.getParameters().getNamed().get("defaultIP"));
        if (this.getParameters().getNamed().containsKey("defaultName")) username.setText(this.getParameters().getNamed().get("defaultName"));
        out.setWrapText(true);
        out.textProperty().addListener((ChangeListener<? super String>) (ObservableValue<? extends String> observable, String old, String n)->{
            out.setScrollTop(Double.MAX_VALUE);
            out.setScrollLeft(Double.MIN_VALUE);
        });
    }

    @Override
    public void stop() {
        if (this.client != null) this.client.disconnect();
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.strings = resources;
    }

    public String getString(String key, Object... params  ) {
        try {
            return MessageFormat.format(this.strings.getString(key), params);
        } catch (MissingResourceException e) {
            return key;
        }
    }

    public String getString(String key) {
        try {
            return this.strings.getString(key);
        } catch (MissingResourceException e) {
            return key;
        }
    }
}
