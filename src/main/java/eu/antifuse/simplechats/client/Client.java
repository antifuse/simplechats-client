package eu.antifuse.simplechats.client;

import eu.antifuse.simplechats.Transmission;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketAddress;

public class Client {
    private final Socket socket;
    private String username;
    private PrintWriter write;
    private ClientReader reader;
    private Controller gui;

    public Client() {
        this.socket = new Socket();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public void connect(SocketAddress ip) {
        try {
            this.socket.connect(ip);
            this.reader = new ClientReader(this.socket,this);
            this.reader.setGui(this.gui);
            this.reader.start();
            this.write = new PrintWriter(this.socket.getOutputStream(), true);
            System.out.println("Requesting name: " + username);
            this.write.println(new Transmission(Transmission.TransmissionType.RQ_NICK, username).serialize());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (!this.socket.isConnected()) return;
        this.write.println(new Transmission(Transmission.TransmissionType.RQ_DISCONNECT).serialize());
        this.write.close();
    }

    public void sendMessage(String content) {
        if (content == null || this.username == null) return;
        if (content.split(" ")[0].equals("/list")) this.sendToServer(Transmission.TransmissionType.RQ_LIST);
        else if (content.split(" ")[0].equals("/name")) {
            if (content.split(" ").length < 2) return;
            this.sendToServer(Transmission.TransmissionType.RQ_NICK, content.split(" ")[1]);
        } else if (content.split(" ")[0].equals("/msg")) {
            this.sendToServer(Transmission.TransmissionType.RQ_DIRECT, content.split(" ")[1], content.substring(6+content.split(" ")[1].length()));
        } else {
            System.out.println("Requesting message " + content);
            this.sendToServer(Transmission.TransmissionType.RQ_SEND, content);
        }
    }

    public void sendToServer(Transmission.TransmissionType type, String ...payload) {
        this.write.println(new Transmission(type, payload).serialize());
    }

    public ClientReader getReader() {
        return reader;
    }

    public void setGui(Controller gui) {
        this.gui = gui;
    }
}
