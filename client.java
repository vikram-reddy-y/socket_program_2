import java.io.*;
import java.net.*;

public class client {


    private static final String HOST = "10.209.220.39";
    private static final int PORT = 5000;

    public static void main(String[] args) {

        try {

            Socket socket = new Socket(HOST, PORT);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in));

            String serverMsg = in.readLine();
            System.out.println(serverMsg);

            String name = console.readLine();
            out.println(name);

            serverMsg = in.readLine();
            if (serverMsg != null && serverMsg.contains("already")) {
                System.out.println(serverMsg);
                socket.close();
                return;
            }

            System.out.println("Connected to chat server.");

            Thread receiveThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (Exception e) {
                    System.out.println("Disconnected from server.");
                }
            });

            receiveThread.start();

            String msg;
            while ((msg = console.readLine()) != null) {

                out.println(msg);

                if (msg.equalsIgnoreCase("bye")) {
                    break;
                }
            }

            socket.close();

        } catch (Exception e) {
            System.out.println("Client error: " + e);
        }
    }
}