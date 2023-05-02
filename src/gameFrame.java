import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class gameFrame extends JFrame {
    private final static Dimension DEFAULT_DIMENSION = new Dimension(400, 600);

    private Board board;
    private P2PGame p2pGame;
    private Player player1;
    private Player player2;
    private Player currentPlayer;

    JPanel mainPanel = new JPanel(new GridBagLayout());
    String[] options = {"Human", "Computer"};
    JComboBox optionList = new JComboBox(options);
    boolean isVisible = false;

    public gameFrame() {
        this(DEFAULT_DIMENSION);
    }

    public gameFrame(Dimension dim) {
        super("Omok");
        setLayout(new BorderLayout());
        configureGui(dim);
        player1 = createPlayer("Player 1", Color.BLACK);
        player2 = createPlayer("Player 2", Color.RED);
        board = new Board(player1, player2);
        if (board.isWonBy(player1)) {
            System.out.println("Player 1 wins!");
        }
        GridBagConstraints gbc = new GridBagConstraints();
        mainPanel.add(board, gbc);
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.setVisible(isVisible);

        setSize(dim);
        setLocationRelativeTo(null);
        setVisible(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void configureGui(Dimension dim) {
        // Top menu Area
        JMenuBar menuBar = new JMenuBar();
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BorderLayout());
        menuPanel.add(new JLabel("     Game"), BorderLayout.WEST);

        // Toolbar
        JToolBar toolbar = new JToolBar();
        JButton btn = new JButton();
        JButton btn2 = new JButton();
        JButton btn3 = new JButton();
        JButton btn4 = new JButton();

        ImageIcon playIcon = new ImageIcon("res/PlayButton2Resized.png");
        ImageIcon aboutIcon = new ImageIcon("res/About.png");
        ImageIcon exitIcon = new ImageIcon("res/exit.png");
        ImageIcon ConnectIcon = new ImageIcon("res/ConnectSmall.png");
        btn.setFocusable(false);
        btn2.setFocusable(false);
        btn3.setFocusable(false);
        btn4.setFocusable(false);

        btn.setIcon(playIcon);
        btn2.setIcon(aboutIcon);
        btn3.setIcon(exitIcon);
        btn4.setIcon(ConnectIcon);

        btn.setToolTipText("Start New Game");
        btn2.setToolTipText("About info");
        btn3.setToolTipText("Exit");
        btn4.setToolTipText("Connect");

        btn4.addActionListener(e -> {
            if (p2pGame == null) {
                int choice = JOptionPane.showOptionDialog(this, "Choose role:", "Role Selection", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new Object[]{"Server", "Client"}, null);
                if (choice == 0 || choice == 1) {
                    String serverIp = JOptionPane.showInputDialog(this, "Enter server IP address:");
                    int serverPort = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter server port:"));
                    if (choice == 0) { // Server
                        try {
                            ServerSocket serverSocket = new ServerSocket(serverPort);
                            System.out.println("Server started on port: " + serverSocket.getLocalPort());
                            System.out.println("Waiting for client to connect...");
                            Socket clientSocket = serverSocket.accept();
                            // Server
                            P2PGame game = new P2PGame(true, clientSocket, this::onConnected, this);
                            p2pGame = game;
                            game.start();
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(this, "Could not start server: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } else { // Client
                        try {
                            Socket socket = new Socket(serverIp, serverPort);
                            P2PGame game = new P2PGame(false, socket, this::onConnected, this);
                            p2pGame = game;
                            game.start();
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(this, "Could not connect to server: " + ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        btn.addActionListener(e -> {
            if (isVisible) {
                int answer = JOptionPane.showConfirmDialog(null, "Are you sure you'd like to start a new game?", "Confirmation", JOptionPane.YES_NO_OPTION);
                if (answer == 0) {
                    board.clear();
                    isVisible = false;
                    mainPanel.setVisible(isVisible);
                    optionList.setEnabled(true);
                }
            }
        });

        btn2.addActionListener(e -> {
            System.out.println("About selected");
            String omokDescription = "<html>Omok is a traditional Korean board game, also known as \"Five in a Row.\"<br>" +
                    "It is played on a grid of intersecting lines, typically 15x15 or 19x19. The game involves<br>" +
                    "two players taking turns placing black and white stones on the board's intersections.<br>" +
                    "The objective is to form an unbroken row of five stones horizontally, vertically, or<br>" +
                    "diagonally. The player who achieves this goal first wins the game. Omok is known for<br>" +
                    "its simplicity and strategic depth, providing an engaging and enjoyable experience<br>" +
                    "for players of all ages.</html>";
            JOptionPane.showMessageDialog(null, omokDescription, "About Omok", JOptionPane.INFORMATION_MESSAGE);
        });

        btn3.addActionListener(e -> {
            this.dispose();
        });

        toolbar.add(btn);
        toolbar.add(btn2);
        toolbar.add(btn3);
        toolbar.add(btn4);
        menuPanel.add(toolbar, BorderLayout.SOUTH);

        // Add Selection
        JPanel selectPanel = new JPanel();
        JButton playButton = new JButton("Play");
        playButton.setFocusable(false);
        playButton.setToolTipText("Start Game");

        // JComboBox
        JLabel opponent = new JLabel("Opponent: ");
        selectPanel.add(playButton);
        selectPanel.add(opponent);
        selectPanel.add(optionList);

        playButton.addActionListener(e -> {
            if (!isVisible) {
                isVisible = true;
                mainPanel.setVisible(isVisible);
                optionList.setEnabled(false);
            }
        });

        optionList.addActionListener(e -> {
            if (optionList.getSelectedItem() == "Computer") {
                System.out.println("Computer was selected (TEST)");
            }
            if (optionList.getSelectedItem() == "Human") {
                System.out.println("Human was selected (TEST)");
            }
        });

        setContentPane(selectPanel);

        menuBar.add(menuPanel);
        setJMenuBar(menuBar);
    }

    private void onConnected() {
        SwingUtilities.invokeLater(() -> {
            board.clear();
            isVisible = true;
            mainPanel.setVisible(isVisible);
            optionList.setEnabled(false);
            board.setMoveListener((x, y) -> {
                if (p2pGame != null && p2pGame.isLocalPlayerTurn()) {
                    p2pGame.sendMove(x, y);
                }
            });
        });
    }

    public Board getBoard() {
        return board;
    }

    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }


    public Player createPlayer(String name, Color color) {
        return new Player(name, color);
    }
}
