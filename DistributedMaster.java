import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DistributedMaster {
    private static final int PORT = 5000;
    private static final String SLAVE_SERVER_ADDRESS = "slave_server_address"; // Replace with actual address
    private static final int SLAVE_SERVER_PORT = 6000; 
    private static final int MAX_THREAD_COUNT = 1;


    public static void main(String[] args) {        
        try (PrintWriter logWriter = new PrintWriter(new FileWriter("response_times.txt"))) {
            logWriter.println("Thread Count, Run 1, Run 2, Run 3, Run 4, Run 5, Average");

            System.out.println("Running experiment with " + MAX_THREAD_COUNT + " threads...");
            ExecutorService pool = Executors.newFixedThreadPool(MAX_THREAD_COUNT);

            long[] runTimes = new long[5];

            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Master server is running...");
                
                for (int i = 0; i < 5; i++) {
                    long startTime = System.currentTimeMillis();
                    Socket clientSocket = serverSocket.accept();
                    pool.execute(() -> sendTask(clientSocket));
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

            //write to the log file
            logWriter.print(MAX_THREAD_COUNT);
            for (long time : runTimes) {
                logWriter.print(", " + time);
            }
            logWriter.println(", " + average);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendTask(Socket clientSocket) {
        try (PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

            //send task to slave server
            Socket slaveSocket = new Socket(SLAVE_SERVER_ADDRESS, SLAVE_SERVER_PORT);
            PrintWriter slaveOut = new PrintWriter(slaveSocket.getOutputStream(), true);
            BufferedReader slaveIn = new BufferedReader(new InputStreamReader(slaveSocket.getInputStream()));

            String task = in.readLine(); //receive task from client
            slaveOut.println(task); //send task and current thread count to slave server

            String result = slaveIn.readLine(); //receive result from slave server
            out.println(result); //send result back to client

            slaveSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
