import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

public class MasterServer {
    private static final int PORT = 5000;
    private static final int MAX_THREAD_COUNT = 1;

    public static void main(String[] args) {

        System.out.println("Running experiment with " + MAX_THREAD_COUNT + " threads...");
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Master server is running...");

            for(int i= 0; i < 5; i++) {
                Socket clientSocket = serverSocket.accept();
                pool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        pool.shutdown();
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            ExecutorService taskPool = Executors.newSingleThreadExecutor();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Read the task from the client
                String task = in.readLine();
                System.out.println("Received task: " + task);
                String[] points = task.split(",");
                int start = Integer.parseInt(points[0]);
                int end = Integer.parseInt(points[1]);

                // Perform the task of finding primes using a separate thread
                Future<String> result = taskPool.submit(() -> {
                    System.out.println("Starting findPrimes task from " + start + " to " + end);
                    return findPrimes(start, end);
                });

                // Send the result back to the client
                out.println(result.get());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                taskPool.shutdown();
            }
        }

        private String findPrimes(int start, int end) {
            StringBuilder primes = new StringBuilder();
            for (int i = start; i <= end; i++) {
                if (check_prime(i)) {
                    primes.append(i);
                }
                // System.out.println("Checking: " + i);
            }
            return primes.toString();
        }

        private boolean check_prime(int n) {
            if (n == 1) { // check if n = 1
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
}
