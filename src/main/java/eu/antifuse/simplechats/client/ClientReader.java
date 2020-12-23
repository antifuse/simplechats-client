package eu.antifuse.simplechats.client;

import eu.antifuse.simplechats.Transmission;

import javax.swing.plaf.basic.BasicButtonUI;
import java.io.*;
import java.net.Socket;

public class ClientReader extends Thread {
    private Controller gui;

    private BufferedReader reader;
    private Socket socket;
    private Client client;

    public ClientReader(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream input = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(input));
        } catch (IOException ex) {
            System.out.println("Error getting input stream: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void run() {
        while (true) {
            try {
                Transmission response = Transmission.deserialize(reader.readLine());
                System.out.println(response);
                this.gui.processMessage(response);

            } catch (Exception ex) {
                System.out.println("Error reading from server: " + ex.getMessage());
                ex.printStackTrace();
                break;
            }
        }
    }

    public void setGui(Controller gui) {
        this.gui = gui;
    }
}
