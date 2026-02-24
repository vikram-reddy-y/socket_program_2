import java.io.*;
import java.net.*;
import java.util.*;

public class server {

    private static final int PORT = 5000;

    private static Map<String, ClientInfo> clients =
            Collections.synchronizedMap(new HashMap<>());

    static class ClientInfo {
        String name;
        InetAddress address;
        int port;
        Date connectedAt;
        PrintWriter out;

        ClientInfo(String name, Socket socket, PrintWriter out) {
            this.name = name;
            this.address = socket.getInetAddress();
            this.port = socket.getPort();
            this.connectedAt = new Date();
            this.out = out;
        }

        public String toString() {
            return "Name: " + name +
                    ", IP: " + address.getHostAddress() +
                    ", Port: " + port +
                    ", Connected At: " + connectedAt;
        }
    }

    public static void main(String[] args) {

        System.out.println("Server running on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();  
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e);
        }
    }

    public static void broadcast(String message) {

        synchronized (clients) {
            for (ClientInfo c : clients.values()) {
                c.out.println(message);
            }
        }

        System.out.println(message);     }

    static class ClientHandler extends Thread {

        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private String clientName;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                out = new PrintWriter(
                        socket.getOutputStream(), true);

                while (true) {

                    out.println("Enter your name:");
                    clientName = in.readLine();

                    if (clientName == null) return;

                    clientName = clientName.trim();

                    synchronized (clients) {
                        if (!clients.containsKey(clientName)) {

                            ClientInfo info =
                                    new ClientInfo(clientName, socket, out);

                            clients.put(clientName, info);

                            System.out.println("Client Registered -> " + info);

                            break;
                        }
                    }

                    out.println("Name already exists. Try another.");
                }

                broadcast(clientName + " joined the chat.");

                String message;

                while ((message = in.readLine()) != null) {

                    if (message.equalsIgnoreCase("bye"))
                        break;

                    broadcast(clientName + ": " + message);
                }

            } catch (Exception e) {
                System.out.println("Client error: " + e);
            } finally {

                try {
                    if (clientName != null) {
                        clients.remove(clientName);
                        broadcast(clientName + " left the chat.");
                    }
                    socket.close();
                } catch (Exception ignored) {}
            }
        }
    }
}