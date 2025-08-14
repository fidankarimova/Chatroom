package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Do you want to start the Server or Client? (Server/Client): ");
        String choice = scanner.nextLine().trim().toUpperCase();

        if (choice.equalsIgnoreCase("Server")) {
            startServer();
        } else if (choice.equalsIgnoreCase("Client")) {
            startClient();
        } else {
            System.out.println("Invalid choice! Please run again.");
        }
    }

    private static void startServer() {
        try {
            ServerSocket serverSocket = new ServerSocket(1234);
            System.out.println("Server started on port 1234...");
            Server server = new Server(serverSocket);
            server.startServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void startClient() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter server IP: ");
        String ip = scanner.nextLine();
        if (ip.trim().isEmpty()) ip = "localhost";

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        try {
            Socket socket = new Socket(ip, 1234);
            Client client = new Client(socket, username);
            System.out.println("Connected to chat server!");
            client.listenForMessage();
            client.sendMessage();
        } catch (IOException e) {
            System.err.println("Could not connect to server at " + ip + ":1234");
            e.printStackTrace();
        }
    }
}





