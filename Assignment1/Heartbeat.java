import java.io.*;
import java.net.*;

public class Heartbeat {
    public static final int PORT = 6000;
    public static final int TIMEOUT = 5000; // Heartbeat timeout 5 sec

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Waiting for Traffic Light Controller to connect");
            Socket clientSocket = serverSocket.accept();
            System.out.println("Traffic Light Controller connected");

            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            final long[] lastHeartbeatTime = {System.currentTimeMillis()};

            // Thread to continuously read heartbeat messages from the controller.
            Thread readerThread = new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        if (message.startsWith("HEARTBEAT")) {
                            System.out.println("Received " + message);
                            lastHeartbeatTime[0] = System.currentTimeMillis(); // Update last heartbeat time
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Exception: " + e.getMessage());
                }
            });

            readerThread.start();

            // Monitor thread to check for missed heartbeats
            Thread monitorThread = new Thread(() -> {
                while (true) {
                    if (System.currentTimeMillis() - lastHeartbeatTime[0] > TIMEOUT) {
                        System.out.println("Traffic Light Controller has stopped responding (missed heartbeat).");
                        // Reset the last heartbeat time to prevent flooding with messages
                        lastHeartbeatTime[0] = System.currentTimeMillis();
                    }

                    try {
                        Thread.sleep(TIMEOUT / 2); // Sleep for half the timeout to check periodically
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });

            monitorThread.start();

            // Keep the program running
            readerThread.join();
            monitorThread.join();

        } catch (IOException | InterruptedException e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }
}
