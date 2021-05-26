package com.naumDeveloper.javaCore3.unit_3.homework_server_IO;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String name;
    private boolean authCheck = false;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            doListen();
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }

    public String getName() {
        return name;
    }

    private synchronized void doListen() {
        new Thread(() -> {
            try {
                doAuth();
               // receiveMessage(); // не влияет

            } catch (Exception e) {
                throw new RuntimeException("SWW", e);
            } finally {
                sendMessage("Client " + this.name + " is logged out");
                server.unsubscribe(this);
            }
        }).start();
    }

    private void doAuth()  {
        try {

            while (true) {
                String credentials = in.readUTF();
                /**
                 * Input credentials sample
                 * "-auth n1@mail.com 1"
                 */
                if (credentials.startsWith("-auth")) {
                    /**
                     * After splitting sample
                     * array of ["-auth", "n1@mail.com", "1"]
                     */
                    String[] credentialValues = credentials.split("\\s");

                    server.getAuthenticationService()
                            .doAuth(credentialValues[1], credentialValues[2])
                            .ifPresentOrElse(
                                    user -> {
                                        if (!server.isLoggedIn(user.getNickname())) {
                                            sendMessage("cmd auth: Status OK");
                                            name = user.getNickname();
                                            server.broadcastMessage(name + " is logged in.");
                                            server.subscribe(this);
                                            authCheck = true;
                                        } else {
                                            sendMessage("Current user is already logged in.");
                                        }
                                    },
                                    new Runnable() {
                                        @Override
                                        public void run() {
                                            sendMessage("No a such user by email and password. (Нет такого пользователя по электронной почте и паролю.)  ");
                                        }
                                    }
                            );
                    if (authCheck) {
                        socket.setSoTimeout(0);
                        return;
                    }
                }
                sendMessage("Authorize before using chat (Авторизуйтесь перед использованием чата )");
            }


        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }




    /**
     * Receives input data from {@link ClientHandler#in} and then broadcast via {@link Server#broadcastMessage(String)}
     */

/** // не влияет
    private void receiveMessage(){
        try {
            while (true) {
                String message = in.readUTF();
                //  Input privateMessage sample -w n1 hello"



                if (message.startsWith("-w")) {
                    String[] privateMessage = message.split("\\s");
                    server.sendPrivateMessage(this.name, privateMessage[1], privateMessage[2]);
                }


                 //Input changeNicknameMessage sample -change newNickname password"


                else if (message.startsWith("-change")) {
                    String[] changeNicknameMessage = message.split("\\s");
                    boolean resMsgFlag = server.changeNickname(this.name, changeNicknameMessage[1], changeNicknameMessage[2]);
                    if (resMsgFlag) {
                        server.sendPrivateMessage("Server", this.name, "Reconnect, to use new nickname");
                    } else server.sendPrivateMessage("Server", this.name, "Wrong password, try again");
                } else if (message.equals("-exit")) {
                    return;
                } else {
                    message = (name + ": " + message);
                    server.broadcastMessage(message);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }

 */


    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            throw new RuntimeException("SWW", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientHandler that = (ClientHandler) o;
        return Objects.equals(server, that.server) &&
                Objects.equals(socket, that.socket) &&
                Objects.equals(in, that.in) &&
                Objects.equals(out, that.out) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(server, socket, in, out, name);
    }
}
