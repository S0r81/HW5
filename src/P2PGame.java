import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class P2PGame {

    private boolean isServer;
    private ServerSocket serverSocket;
    private Socket clientSocket;
    private Runnable onConnectedCallback;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private gameFrame gameFrame;
    private Board board;
    private Player localPlayer;
    private Player remotePlayer;
    private boolean isLocalPlayerTurn;

    public P2PGame(boolean isServer, Socket clientSocket, Runnable onConnectedCallback, gameFrame gameFrame) {
        this.isServer = isServer;
        this.clientSocket = clientSocket;
        this.onConnectedCallback = onConnectedCallback;
        this.gameFrame = gameFrame;
        board = gameFrame.getBoard();
        board.setMoveListener((x, y) -> {
            if (isLocalPlayerTurn) {
                sendMove(x, y);
            }
        });
        if (isServer) {
            localPlayer = gameFrame.getPlayer1();
            remotePlayer = gameFrame.getPlayer2();
            isLocalPlayerTurn = true;
        } else {
            localPlayer = gameFrame.getPlayer2();
            remotePlayer = gameFrame.getPlayer1();
            isLocalPlayerTurn = false;
        }
    }


    public void setOnConnectedCallback(Runnable onConnectedCallback) {
        this.onConnectedCallback = onConnectedCallback;
    }

    public void start() {
        new Thread(() -> {
            if (isServer) {
                startServer();
            } else {
                startClient();
            }
        }).start();
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
        if (message.startsWith("MOVE:")) {
            String[] parts = message.split(":");
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            processMove(x, y);
        }
    }

    private void processMove(int x, int y) {
        board.placeStone(x, y, remotePlayer);
        board.repaint();
        board.checkWinAndShowMessage();
        isLocalPlayerTurn = !isLocalPlayerTurn;
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

    public boolean isLocalPlayerTurn() {
        return isLocalPlayerTurn;
    }

    public void sendMove(int x, int y) {
        if (outputStream != null) {
            sendMessage(x + "," + y);
        } else {
            System.err.println("Output stream not initialized. Cannot send move.");
        }
    }
}
