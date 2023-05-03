import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class P2PGame {

    private int userPort;
    private String userIp;
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
    private NetworkAdapter networkAdapter;
    private boolean serverStarted;

    public P2PGame(Socket socket) {
        this.networkAdapter = new NetworkAdapter(socket);
        isServer = false;
        initGame();
    }

    public P2PGame(boolean isServer, String userIp, int userPort, Runnable onConnectedCallback, gameFrame gameFrame) {
        this.isServer = isServer;
        this.userIp = userIp;
        this.userPort = userPort;
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
        if (isServer && !serverStarted) {
            try {
                serverSocket = new ServerSocket(userPort); // Use the user-defined port
                System.out.println("Server started on port: " + serverSocket.getLocalPort());
                System.out.println("Waiting for client to connect...");
                clientSocket = serverSocket.accept();
                networkAdapter = new NetworkAdapter(clientSocket); // Initialize the networkAdapter
                initGame();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                if (onConnectedCallback != null) {
                    onConnectedCallback.run();
                }
                serverStarted = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Client side
            try {
                clientSocket = new Socket(userIp, userPort); // Use the user-defined IP and port
                networkAdapter = new NetworkAdapter(clientSocket); // Initialize the networkAdapter
                initGame();
                System.out.println("Connected to server: " + clientSocket.getInetAddress());
                if (onConnectedCallback != null) {
                    onConnectedCallback.run();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }




    private void initGame() {
        try {
            this.inputStream = new DataInputStream(clientSocket.getInputStream());
            this.outputStream = new DataOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        networkAdapter.setMessageListener((type, x, y) -> {
            switch (type) {
                case PLAY:
                    // Handle PLAY message
                    break;
                case PLAY_ACK:
                    // Handle PLAY_ACK message
                    break;
                case MOVE:
                    // Handle MOVE message
                    break;
                case MOVE_ACK:
                    // Handle MOVE_ACK message
                    break;
                case QUIT:
                    // Handle QUIT message
                    break;
                case CLOSE:
                    // Handle CLOSE message
                    break;
                case UNKNOWN:
                    // Handle UNKNOWN message
                    break;
            }
        });
        networkAdapter.receiveMessagesAsync();
        startReading();
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
            String[] parts = message.substring(5).split(",");
            int x = Integer.parseInt(parts[0]);
            int y = Integer.parseInt(parts[1]);
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

    public boolean isLocalPlayerTurn() {
        return isLocalPlayerTurn;
    }

    public void sendMove(int x, int y) {
        if (outputStream != null) {
            sendMessage("MOVE:" + x + "," + y);
        } else {
            System.err.println("Output stream not initialized. Cannot send move.");
        }
    }

    public void close() {
        networkAdapter.close();
    }

}
