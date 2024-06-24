import java.io.*;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        int start = 1;
        int end = 100_000_000;

        try (Socket socket = new Socket("localhost", 5000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Send the task to the master server
            long startTime = System.currentTimeMillis();
            out.println(start + "," + end);

            // Read and ignore the result from the master server (for timing purposes)
            in.readLine();
            long endTime = System.currentTimeMillis();
            System.out.println("Response time: " + (endTime - startTime) + " ms");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
