import java.io.*;
import java.net.*;

public class client {

    //  Change this to server IP when using 2 laptops
    private static final String HOST = "172.20.10.5";
    private static final int PORT = 6031;

    public static void main(String[] args) {

        try {

            Socket socket = new Socket(HOST, PORT);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            BufferedReader console = new BufferedReader(
                    new InputStreamReader(System.in));

            // Thread to receive server responses
            Thread receiveThread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        System.out.println(msg);
                    }
                } catch (Exception e) { }
            });

            receiveThread.start();

            String msg;

            while ((msg = console.readLine()) != null) {

                if (msg.equalsIgnoreCase("exit")) {
                    out.println("exit");
                    break;
                }

                out.println(msg);
            }

            socket.close();

        } catch (Exception e) {
            System.out.println("Client error: " + e);
        }
    }
}
