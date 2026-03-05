import java.io.*;
import java.net.*;
import java.util.*;

public class server {

    private static final int PORT = 6031;

    // Data structure to store connected clients
    private static List<ClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {

        System.out.println("Server running on port " + PORT);
        System.out.println("Type 'list' to display connected clients.\n");

        // ✅ Thread to read SERVER console commands
        new Thread(() -> {
            try {
                BufferedReader console =
                        new BufferedReader(new InputStreamReader(System.in));

                String command;

                while ((command = console.readLine()) != null) {

                    if (command.equalsIgnoreCase("list")) {
                        displayClients();
                    }
                }

            } catch (IOException e) {
                System.out.println("Console error: " + e);
            }
        }).start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();   // Multithreading
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e);
        }
    }

    // ✅ Display connected clients on server
    public static void displayClients() {

        System.out.println("\n---- Connected Clients ----");

        synchronized (clients) {
            if (clients.isEmpty()) {
                System.out.println("No clients connected.");
            } else {
                for (ClientHandler c : clients) {
                    if (c.clientName != null)
                        System.out.println(c.clientName);
                }
            }
        }

        System.out.println("----------------------------\n");
    }

    // ✅ Check duplicate names
    public static boolean nameExists(String name) {
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (c.clientName != null &&
                        c.clientName.equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    // ✅ Thread class (One per client)
    static class ClientHandler extends Thread {

        Socket socket;
        BufferedReader in;
        PrintWriter out;
        String clientName;

        ClientHandler(Socket s) {
            socket = s;
        }

        public void run() {

            try {

                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(
                        socket.getOutputStream(), true);

                // Ask for unique name
                while (true) {

                    out.println("Enter your name:");
                    clientName = in.readLine();

                    if (clientName == null)
                        return;

                    if (!nameExists(clientName))
                        break;

                    out.println("Name already exists. Try another.");
                }

                clients.add(this);

                // Show join ONLY on server
                System.out.println(clientName + " joined the chat.");

                String msg;

                while ((msg = in.readLine()) != null) {

                    if (msg.equalsIgnoreCase("exit"))
                        break;

                    // ✅ Show messages ONLY on server
                    System.out.println("Message from "
                            + clientName + ": " + msg);

                    // Optional acknowledgement to sender
                    out.println("Server received your message.");
                }

            } catch (Exception e) {
                System.out.println("Client error: " + e);
            } finally {

                try {
                    clients.remove(this);
                    System.out.println(clientName + " left the chat.");
                    socket.close();
                } catch (Exception e) { }
            }
        }
    }
}