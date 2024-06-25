import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SlaveServer {
    private static final int PORT = 6000;
    private static final int MAX_THREAD_COUNT = 1;

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Slave server is running...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                executor.submit(() -> handleClientRequest(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown(); // Shutdown the executor service when done
        }
    }

    private static void handleClientRequest(Socket clientSocket) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {
    
            // Read task from master server
            String task = in.readLine();
            String[] points = task.split(",");
            int start = Integer.parseInt(points[0]);
            int end = Integer.parseInt(points[1]);
    
            // Find primes
            String result = findPrimes(start, end);
    
            // Send the result back to the master server
            out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String findPrimes(int start, int end) {
        StringBuilder primes = new StringBuilder();
        for (int i = start; i <= end; i++) {
            if (check_prime(i)) {
                primes.append(i).append(" ");
            }
        }
        return primes.toString();
    }

    private static boolean check_prime(int n) {
        if (n == 1) {
            return false;
        }
        for (int i = 2; i * i <= n; i++) {
            if (n % i == 0) {
                return false;
            }
        }
        return true;
    }
}