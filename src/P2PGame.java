import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class P2PGame extends NetworkAdapter {

    private boolean isServer;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Runnable onConnectedCallback;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;

    public P2PGame(boolean isServer, Socket socket, Runnable onConnectedCallback) {
        super(socket);
        this.isServer = isServer;
        this.onConnectedCallback = onConnectedCallback;
    }

    public void setOnConnectedCallback(Runnable onConnectedCallback) {
        this.onConnectedCallback = onConnectedCallback;
    }

    public void start() {
        if (isServer) {
            startServer();
        } else {
            startClient();
        }
    }

    private void init(Socket socket) {
        try {
            this.clientSocket = socket;
            this.inputStream = new DataInputStream(clientSocket.getInputStream());
            this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
            startReading();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void startReading() {
        Thread readingThread = new Thread(() -> {
            try {
                while (true) {
                    String message = inputStream.readUTF();
                    onMessageReceived(message);
                }
            } catch (EOFException e) {
                System.out.println("Connection closed by the remote peer.");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        readingThread.start();
    }

    protected void onMessageReceived(String message) {
        System.out.println("Received message: " + message);
        // Add your message handling code here
    }

    private void sendMessage(String message) {
        try {
            outputStream.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startServer() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter port to listen on: ");
        int port = scanner.nextInt();

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started on port: " + port);
            System.out.println("Waiting for client to connect...");
            clientSocket = serverSocket.accept();
            init(clientSocket);
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            if (onConnectedCallback != null) {
                onConnectedCallback.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startClient() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter server IP address: ");
        String serverIp = scanner.nextLine();
        System.out.println("Enter server port: ");
        int serverPort = scanner.nextInt();

        try {
            clientSocket = new Socket(serverIp, serverPort);
            init(clientSocket); // add this line to initialize the socket
            System.out.println("Connected to server: " + serverIp + ":" + serverPort);
            if (onConnectedCallback != null) {
                onConnectedCallback.run();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
