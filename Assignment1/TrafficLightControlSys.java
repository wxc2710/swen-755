import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

public class TrafficLightControlSys {
    private static final String MONITOR_HOST = "localhost";
    private static final int MONITOR_PORT = 6000; // port number
    private static final int LIGHT_INTERVAL = 2000; // light switch interval (2 seconds)

    // Traffic light states
    enum Light {
        RedLight, GreenLight, YellowLight
    }

    public static void main(String[] args) {
        try (Socket socket = new Socket(MONITOR_HOST, MONITOR_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            Random random = new Random();
            Light currentLight = Light.RedLight; // initialize with red light
            int heartbeatCount = 0;
            boolean failureOccurred = false; // flag to simulate failure

            while (true) {
                // Simulate random failure with a 20% chance
                if (!failureOccurred && random.nextInt(100) < 20) {
                    failureOccurred = true;
                    System.out.println("A failure occurred in the Traffic Light System.");
                    // After failure, stop sending heartbeats but continue running
                }

                // Only send heartbeat if no failure occurred
                if (!failureOccurred) {
                    out.println("HEARTBEAT " + heartbeatCount);
                    System.out.println("Sent heartbeat " + heartbeatCount);
                    heartbeatCount++;
                }

                // Switch traffic lights
                if (!failureOccurred) {
                    switch (currentLight) {
                        case RedLight:
                            currentLight = Light.GreenLight;
                            System.out.println("Switching to GREEN light");
                            break;
                        case GreenLight:
                            currentLight = Light.YellowLight;
                            System.out.println("Switching to YELLOW light");
                            break;
                        case YellowLight:
                            currentLight = Light.RedLight;
                            System.out.println("Switching to RED light");
                            break;
                    }
                }

                Thread.sleep(LIGHT_INTERVAL);
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Exception: " + e.getMessage());
        }
    }
}
