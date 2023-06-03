package liteweb;

import liteweb.http.Request;
import liteweb.http.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Server {

    private static final Logger log = LogManager.getLogger(Server.class);
    private static final int DEFAULT_PORT = 8080;
    private static final int WORKER_NUM = 2;
    private static final BlockingQueue<Socket> ACCEPTED_SOCKET_QUEUE = new ArrayBlockingQueue<>(200);
    private static final ExecutorService WORKER_EXEC = Executors.newFixedThreadPool(WORKER_NUM);

    public static void main(String[] args) throws IOException {

        new Server().startListen(getValidPortParam(args));
    }

    public void startListen(int port) throws IOException {

        try (ServerSocket socket = new ServerSocket(port)) {
            log.info("Web server listening on port {} (press CTRL-C to quit)", port);

            // start workers
            for (int i = 0; i < WORKER_NUM; i++) {
                WORKER_EXEC.execute(Server::consume);
            }

            // accept requests
            while (true) {
                accept(socket);
            }
        }
    }

    public static void consume() {
        while (true) {
            try (Socket clientSocket = ACCEPTED_SOCKET_QUEUE.take();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                List<String> requestContent = new ArrayList<>();
                String temp = reader.readLine();
                while (temp != null && temp.length() > 0) {
                    requestContent.add(temp);
                    temp = reader.readLine();
                }
                Request req = new Request(requestContent);
                Response res = new Response(req);
                res.write(clientSocket.getOutputStream());

                TimeUnit.NANOSECONDS.sleep(1);
            } catch (IOException | InterruptedException e) {
                log.error("Exception", e);
            }
        }
    }

    public static void accept(ServerSocket socket) {
        try {
            ACCEPTED_SOCKET_QUEUE.put(socket.accept());
        } catch (IOException | InterruptedException e) {
            log.error("Exception", e);
        }
    }

    /**
     * Parse command line arguments (string[] args) for valid port number
     *
     * @return int valid port number or default value (8080)
     */
    static int getValidPortParam(String[] args) throws NumberFormatException {
        if (args.length > 0) {
            int port = Integer.parseInt(args[0]);
            if (port > 0 && port < 65535) {
                return port;
            } else {
                throw new NumberFormatException("Invalid port! Port value is a number between 0 and 65535");
            }
        }
        return DEFAULT_PORT;
    }
}
