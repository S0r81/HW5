import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Dimension dim = new Dimension(400, 600);
        SwingUtilities.invokeLater(() -> {
            new gameFrame(dim);
        });
    }
}
