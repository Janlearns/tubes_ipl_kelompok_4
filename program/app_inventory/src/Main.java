import com.formdev.flatlaf.FlatLightLaf;
import view.LoginFrame;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
            // UI Global Tweaks
            UIManager.put("Button.arc", 20);
            UIManager.put("Component.arc", 20);

            java.awt.EventQueue.invokeLater(() -> {
                new LoginFrame().setVisible(true);
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}