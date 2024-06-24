import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MasterServer {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        int[] threadCounts = {1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024};
        
        try (PrintWriter logWriter = new PrintWriter(new FileWriter("response_times.txt"))) {
            logWriter.println("Thread Count, Run 1, Run 2, Run 3, Run 4, Run 5, Average");

            for (int threadCount : threadCounts) {
                System.out.println("Running experiment with " + threadCount + " threads...");
                ExecutorService pool = Executors.newFixedThreadPool(threadCount);

                long[] runTimes = new long[5];

                try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                    System.out.println("Master server is running...");
                    for (int i = 0; i < 5; i++) {
                        long startTime = System.currentTimeMillis();
                        Socket clientSocket = serverSocket.accept();
                        pool.execute(new ClientHandler(clientSocket));
                        long endTime = System.currentTimeMillis();
                        runTimes[i] = endTime - startTime;
                        System.out.println("Run " + (i + 1) + " completed in " + runTimes[i] + " ms");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                pool.shutdown();

                long sum = 0;
                for (long time : runTimes) {
                    sum += time;
                }
                long average = sum / runTimes.length;

                // Write to the log file
                logWriter.print(threadCount);
                for (long time : runTimes) {
                    logWriter.print(", " + time);
                }
                logWriter.println(", " + average);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

                // Read the task from the client
                String task = in.readLine();
                String[] points = task.split(",");
                int start = Integer.parseInt(points[0]);
                int end = Integer.parseInt(points[1]);

                // Perform the task of finding primes
                String result = findPrimes(start, end);

                // Send the result back to the client
                out.println(result);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private String findPrimes(int start, int end) {
            StringBuilder primes = new StringBuilder();
            for (int i = start; i <= end; i++) {
                if (check_prime(i)) {
                    primes.append(i).append(" ");
                }
            }
            return primes.toString();
        }

        private boolean check_prime(int n) {
            if (n == 1) { //check if n = 1
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
