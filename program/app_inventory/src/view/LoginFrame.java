package view;

import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.extras.FlatSVGIcon; // Opsional jika pakai SVG
import dao.InventoryDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class LoginFrame extends JFrame {

    private final Color ACCENT_COLOR = new Color(79, 70, 229); // Indigo
    private final Color TEXT_MAIN = new Color(15, 23, 42);     // Slate 900
    private final Color BG_WINDOW = new Color(241, 245, 249);  // Slate 100

    public LoginFrame() {
        setupWindow();
        initUI();
    }

    private void setupWindow() {
        setTitle("INV-PRO | Authentication");
        setSize(450, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initUI() {
        // Root Panel dengan Gradient Halus
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_WINDOW);

        // --- Card Panel ---
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 40");
        card.setBorder(new EmptyBorder(50, 40, 50, 40));
        card.setPreferredSize(new Dimension(380, 500));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1;

        // 1. Logo
        JLabel lblLogo = new JLabel("INV-PRO");
        lblLogo.setFont(new Font("Inter", Font.BOLD, 32));
        lblLogo.setForeground(TEXT_MAIN);
        lblLogo.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        card.add(lblLogo, gbc);

        // 2. Subtitle
        JLabel lblDesc = new JLabel("Enterprise Inventory Control");
        lblDesc.setFont(new Font("Inter", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(100, 116, 139));
        lblDesc.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 45, 0);
        card.add(lblDesc, gbc);

        // 3. Username Label & Field
        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Inter", Font.BOLD, 12));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 5, 8, 0);
        card.add(lblUser, gbc);

        JTextField txtUser = new JTextField();
        txtUser.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "username");
        txtUser.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc: 15;" +
                "height: 45;" +
                "background: #f8fafc;" +
                "focusColor: #4f46e5;" +
                "borderColor: #e2e8f0;");
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        card.add(txtUser, gbc);

        // 4. Password Label & Field
        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Inter", Font.BOLD, 12));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 5, 8, 0);
        card.add(lblPass, gbc);

        JPasswordField txtPass = new JPasswordField();
        txtPass.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "••••••••");
        txtPass.putClientProperty(FlatClientProperties.STYLE, "" +
                "arc: 15;" +
                "height: 45;" +
                "background: #f8fafc;" +
                "focusColor: #4f46e5;" +
                "borderColor: #e2e8f0;" +
                "showRevealButton: true;");
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 35, 0);
        card.add(txtPass, gbc);

        // 5. Login Button
        JButton btnLogin = new JButton("Sign In Securely");
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setFont(new Font("Inter", Font.BOLD, 14));
        btnLogin.putClientProperty(FlatClientProperties.STYLE, "" +
                "background: #4f46e5;" +
                "foreground: #ffffff;" +
                "arc: 15;" +
                "borderWidth: 0;" +
                "focusWidth: 0;");
        btnLogin.setPreferredSize(new Dimension(0, 50));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 0, 0);
        card.add(btnLogin, gbc);

        // 6. Action Logic
        btnLogin.addActionListener(e -> {
            String u = txtUser.getText();
            String p = new String(txtPass.getPassword());
            if (u.isEmpty() || p.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Fields cannot be empty", "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int adminId = new InventoryDAO().getAdminId(u, p);
            if (adminId != -1) {
                new MainFrame(adminId).setVisible(true);
                this.dispose();
            } else {
                showError("Invalid username or password.");
            }
        });

        // 7. Footer
        JLabel lblFooter = new JLabel("© 2026 Powered by Kelompok 4");
        lblFooter.setFont(new Font("Inter", Font.PLAIN, 11));
        lblFooter.setForeground(new Color(148, 163, 184));
        lblFooter.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridy = 7;
        gbc.insets = new Insets(40, 0, 0, 0);
        card.add(lblFooter, gbc);

        root.add(card);
        add(root);
    }

    private void showError(String msg) {
        // Dialog modern kustom bisa ditambahkan di sini
        JOptionPane.showMessageDialog(this, msg, "Auth Error", JOptionPane.ERROR_MESSAGE);
    }
}