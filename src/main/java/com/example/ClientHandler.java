package com.example;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ClientHandler implements Runnable{
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    public static HashMap<String, ArrayList<ClientHandler>> groups = new HashMap<>();

    private Socket socket;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String clientUsername;
    private String currentGroup = null;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("Server: " + clientUsername + " has entered the chat");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String messageFromClient;

        while (socket.isConnected()) {
            try {
                messageFromClient = bufferedReader.readLine();
                if (messageFromClient == null) {
                    break;
                }
                if (messageFromClient.equalsIgnoreCase("/quit")) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                    break;
                }
                else if (messageFromClient.equalsIgnoreCase("/help")) {
                    showHelp();
                }
                else if (messageFromClient.equalsIgnoreCase("/groups")) {
                    listGroups();
                }
                else if (messageFromClient.equalsIgnoreCase("/users")) {
                    listOnlineUsers();
                }
                else if (messageFromClient.startsWith("/w ")) {
                    String[] parts = messageFromClient.split(" ", 3);
                    if (parts.length >= 3) {
                        String targetUsername = parts[1];
                        String privateMessage = parts[2];
                        sendPrivateMessage(targetUsername, privateMessage);
                    } else {
                        bufferedWriter.write("Usage: /w <username> <message>");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }
                else if (messageFromClient.startsWith("/createGroup ")) {
                    String[] parts = messageFromClient.trim().split(" ", 13);
                    if (parts.length >= 2) {
                        String groupName = parts[1];
                        createGroup(groupName);
                    } else {
                        bufferedWriter.write("Usage: /createGroup <groupname>");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }
                else if (messageFromClient.startsWith("/join ")) {
                    String[] parts = messageFromClient.split(" ", 2);
                    if (parts.length >= 2) {
                        String groupName = parts[1];
                        joinGroup(groupName);
                    } else {
                        bufferedWriter.write("Usage: /join <groupname>");
                        bufferedWriter.newLine();
                        bufferedWriter.flush();
                    }
                }
                else if (messageFromClient.equalsIgnoreCase("/quitGroup")) {
                    quitGroup();
                }
                else {
                    if (currentGroup != null) {
                        broadcastToGroup(currentGroup, clientUsername + ": " + messageFromClient);
                    } else {
                        broadcastMessage(clientUsername + ": " + messageFromClient);
                    }
                }
            }
            catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;
            }
        }
    }

    private void showHelp() {
        try {
            bufferedWriter.write("/w - private message");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void createGroup(String groupName) {
        try {
            if (groups.containsKey(groupName)) {
                bufferedWriter.write("Group '" + groupName + "' already exists");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            } else {
                groups.put(groupName, new ArrayList<>());
                bufferedWriter.write("Group '" + groupName + "' created successfully");
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void joinGroup(String groupName) {
        try {
            if (!groups.containsKey(groupName)) {
                bufferedWriter.write("Group '" + groupName + "' does not exist");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                return;
            }

            if (currentGroup != null) {
                quitGroup();
            }

            currentGroup = groupName;
            groups.get(groupName).add(this);
            bufferedWriter.write("Joined group '" + groupName + "'");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            broadcastToGroup(groupName, "Server: " + clientUsername + " joined the group");
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void quitGroup() {
        try {
            if (currentGroup == null) {
                bufferedWriter.write("You are not in any group");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                return;
            }

            groups.get(currentGroup).remove(this);

            broadcastToGroup(currentGroup, "Server: " + clientUsername + " left the group");

            bufferedWriter.write("Left group '" + currentGroup + "'");
            bufferedWriter.newLine();
            bufferedWriter.flush();

            currentGroup = null;
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    private void broadcastToGroup(String groupName, String message) {
        if (groups.containsKey(groupName)) {
            for (ClientHandler clientHandler : groups.get(groupName)) {
                try {
                    if (!clientHandler.clientUsername.equals(clientUsername)) {
                        clientHandler.bufferedWriter.write("[" + groupName + "] " + message);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(socket, bufferedReader, bufferedWriter);
                }
            }
        }
    }

    private void listOnlineUsers() {
        try {
            bufferedWriter.write("Online users: ");
            for (int i = 0; i < clientHandlers.size(); i++) {
                bufferedWriter.write(clientHandlers.get(i).clientUsername);
                if (i < clientHandlers.size() - 1) {
                    bufferedWriter.write(", ");
                }
            }
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void listGroups() {
        try {
            if (groups.isEmpty()) {
                bufferedWriter.write("No groups available");
            } else {
                bufferedWriter.write("Available groups: ");
                String[] groupNames = groups.keySet().toArray(new String[0]);
                for (int i = 0; i < groupNames.length; i++) {
                    bufferedWriter.write(groupNames[i] + " (" + groups.get(groupNames[i]).size() + " members)");
                    if (i < groupNames.length - 1) {
                        bufferedWriter.write(", ");
                    }
                }
            }
            bufferedWriter.newLine();
            bufferedWriter.flush();
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void sendPrivateMessage(String targetUsername, String privateMessage) throws IOException {
        boolean found = false;
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler.clientUsername.equalsIgnoreCase(targetUsername)) {
                clientHandler.bufferedWriter.write("Private message from " + clientUsername + ": " + privateMessage);
                clientHandler.bufferedWriter.newLine();
                clientHandler.bufferedWriter.flush();
                found = true;
                break;
            }
        }
        if (!found) {
            bufferedWriter.write("User '" + targetUsername + "' not found");
            bufferedWriter.newLine();
            bufferedWriter.flush();
        }
    }

    public void broadcastMessage(String messageToSend) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.clientUsername.equals(clientUsername)) {
                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }

    public void removeClientHandler() {
        clientHandlers.remove(this);

        if (currentGroup != null) {
            groups.get(currentGroup).remove(this);
            broadcastToGroup(currentGroup, "Server: " + clientUsername + " disconnected");
        }

        System.out.println(clientUsername + " disconnected");
        broadcastMessage("Server: " + clientUsername + " has left the chat");
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        removeClientHandler();
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
}