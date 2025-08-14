package com.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String username;
    private boolean isAuthenticated = false;

    public Client(Socket socket, String username) {
        try {
            this.socket = socket;
            this.username = username;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            bufferedWriter.write(username);
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in);

            System.out.println("Type /help for available commands");

            while (socket.isConnected()) {
                String messageToSend = scanner.nextLine();

                if (messageToSend.equalsIgnoreCase("/quit")) {
                    bufferedWriter.write("/quit");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    System.exit(0);
                }

                if (messageToSend.equalsIgnoreCase("/help")) {
                    bufferedWriter.write("/help");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }

                if (messageToSend.equalsIgnoreCase("/users")) {
                    bufferedWriter.write("/users");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }

                if (messageToSend.equalsIgnoreCase("/groups")) {
                    bufferedWriter.write("/groups");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }

                if (messageToSend.startsWith("/w ")) {
                    bufferedWriter.write(messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }

                if (messageToSend.startsWith("/createGroup ")) {
                    bufferedWriter.write(messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }

                if (messageToSend.startsWith("/join ")) {
                    bufferedWriter.write(messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }

                if (messageToSend.equalsIgnoreCase("/quitGroup")) {
                    bufferedWriter.write("/quitGroup");
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                    continue;
                }

                if (!messageToSend.trim().isEmpty()) {
                    bufferedWriter.write(messageToSend);
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
            System.exit(0);
        }
    }

    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromServer;

                while (socket.isConnected()) {
                    try {
                        messageFromServer = bufferedReader.readLine();
                        if (messageFromServer != null) {
                            System.out.println(messageFromServer);

                            if (messageFromServer.contains("Authentication successful")) {
                                isAuthenticated = true;
                            }
                        }
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                        break;
                    }
                }
            }
        }).start();
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter server IP: ");
        String ip = scanner.nextLine();
        if (ip.trim().isEmpty()) {
            ip = "localhost";
        }

        System.out.print("Enter a username: ");
        String username = scanner.nextLine();

        try {
            Socket socket = new Socket(ip, 1234);
            Client client = new Client(socket, username);

            System.out.println("Connected to chat server!");
            client.listenForMessage();
            client.sendMessage();
        } catch (IOException e) {
            System.err.println("Could not connect to server. Make sure the server is running on " + ip + ":1234");
            e.printStackTrace();
        }
    }
}