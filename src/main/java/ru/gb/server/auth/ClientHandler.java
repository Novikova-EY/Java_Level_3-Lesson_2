package ru.gb.server.auth;

import ru.gb.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler {
    private final Server server;
    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private String name;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            new Thread(new Runnable() {
                @Override
                public void run() {
                    listen();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }

    private void listen() {
        try {
            doAuth();
            readMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doAuth() throws IOException {
        while (true) {
            String input = in.readUTF();
            if (input.startsWith("-auth")) {
                String[] credentials = input.split("\\s");
                String nicknameCheck = null;
                try {
                    nicknameCheck = AuthenticationService.getNickByLoginPass(credentials[1], credentials[2]);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (nicknameCheck != null) {
                    if (server.isNicknameFree(nicknameCheck)) {
                        sendMessage("CMD: auth is ok");
                        name = nicknameCheck;
                        server.subscribe(this);
                        server.broadcast(name + " logged in.");
                        return;
                    } else {
                        sendMessage("Current user is already logged-in.");
                    }
                } else {
                    sendMessage("Unknown user. Incorrect login/password");
                }
            } else {
                sendMessage("Invalid authentication request.");
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void readMessage() throws IOException {
        while (true) {
            String message = in.readUTF();
            if (message.startsWith("/")) {
                if (message.equals("/end")) {
                    server.unsubscribe(this);
                }
                if (message.startsWith("/w ")) {
                    String[] tokens = message.split("\\s");
                    String nick = tokens[1];
                    String msg = message.substring(4 + nick.length());
                    server.sendMsgToClient(this, nick, msg);
                }
                if (message.startsWith("/change ")) {
                    String[] tokens1 = message.split("\\s");
                    String newNickname = tokens1[1];
                    try {
                        server.changeNick(getName(), newNickname);
                        server.broadcast(name + ": Your nickname has changed on " + newNickname);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                server.broadcast(name + ": " + message);
            }
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
