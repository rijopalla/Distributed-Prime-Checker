import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class DistributedMaster {
    private static final int PORT = 5000;
    private static final String SLAVE_SERVER_ADDRESS = "address"; // Replace with actual address
    private static final int SLAVE_SERVER_PORT = 6000; 
    private static final int MAX_THREAD_COUNT = 1;

    public static void main(String[] args) {
        System.out.println("Running experiment with " + MAX_THREAD_COUNT + " threads...");
        ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Master server is running...");

            for(int i = 0; i < 5; i++) {
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
                int mid = start + (end - start) / 2; // Calculate mid-point

                // Perform the task of finding primes using a separate thread
                Future<String> result = taskPool.submit(() -> {
                    System.out.println("Starting findPrimes task from " + start + " to " + mid);
                    return findPrimes(start, mid);
                });

                // Send the remaining task to the slave server
                String slaveResult = sendTask(mid + 1, end);

                // Combine results
                String finalResult = result.get() + slaveResult;

                // Send the result back to the client
                out.println(finalResult);
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
                    primes.append(i).append(",");
                }
            }
            return primes.toString();
        }

        private boolean check_prime(int n) {
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

    private static String sendTask(int start, int end) {
        try (Socket slaveSocket = new Socket(SLAVE_SERVER_ADDRESS, SLAVE_SERVER_PORT);
             PrintWriter slaveOut = new PrintWriter(slaveSocket.getOutputStream(), true);
             BufferedReader slaveIn = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()))) {

            // Prepare the task for the slave server
            String task = start + "," + end;
            
            // Send task to slave server
            slaveOut.println(task);
            
            // Receive result from slave server
            String result = slaveIn.readLine();
            System.out.println("Result from slave server: " + result);
            
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
