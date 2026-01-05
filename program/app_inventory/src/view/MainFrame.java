package view;

import com.formdev.flatlaf.FlatClientProperties;
import config.Config;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class MainFrame extends JFrame {
    private int currentAdminId;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    private JLabel lblTotal, lblDipinjam;

    // Premium Color Palette
    private final Color SIDEBAR_COLOR = new Color(15, 23, 42);
    private final Color ACCENT_COLOR = new Color(79, 70, 229);
    private final Color BG_SOFT = new Color(248, 250, 252);
    private final Color SUCCESS_COLOR = new Color(16, 185, 129);
    private final Color DANGER_COLOR = new Color(239, 68, 68);

    public MainFrame(int adminId) {
        this.currentAdminId = adminId;
        setupWindow();
        initUI();
        refreshDashboardStats();
    }

    private void setupWindow() {
        setTitle("INVENTORY MANAGEMEMT | Admin Session: " + currentAdminId);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1280, 800));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // --- SIDEBAR NAVIGATION ---
        JPanel sidebar = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 10));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(260, 0));

        JLabel logo = new JLabel("INV-MNG");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Inter", Font.BOLD, 28));
        logo.setBorder(new EmptyBorder(50, 0, 60, 0));
        sidebar.add(logo);

        sidebar.add(createNavBtn("Dashboard", "DASHBOARD"));

        sidebar.add(Box.createVerticalStrut(250));
        sidebar.add(createNavBtn("Sign Out", "LOGOUT"));

        add(sidebar, BorderLayout.WEST);

        // --- CONTENT AREA ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_SOFT);

        cardPanel.add(createDashboardPage(), "DASHBOARD");

        add(cardPanel, BorderLayout.CENTER);
    }

    // --- PAGE GENERATORS ---

    private JPanel createDashboardPage() {
        JPanel p = new JPanel(new BorderLayout(30, 30));
        p.setBackground(BG_SOFT);
        p.setBorder(new EmptyBorder(50, 60, 50, 60));

        JPanel stats = new JPanel(new GridLayout(1, 2, 40, 0));
        stats.setOpaque(false);
        lblTotal = new JLabel("0"); lblDipinjam = new JLabel("0");
        stats.add(createStatCard("TOTAL ASSETS", lblTotal, ACCENT_COLOR));
        stats.add(createStatCard("ACTIVE LOANS", lblDipinjam, new Color(245, 158, 11)));

        p.add(stats, BorderLayout.NORTH);
        JLabel welcome = new JLabel("<html><div style='text-align: center'><h1>System Overview</h1><p>Real-time inventory monitoring and transaction control.</p></div></html>");
        welcome.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(welcome, BorderLayout.CENTER);

        return p;
    }

    // --- REFRESH DATA METHODS ---

    private void refreshDashboardStats() {
        try (Connection conn = Config.configDB()) {
            Statement st = conn.createStatement();
            ResultSet rs = st.executeQuery("SELECT COUNT(*), SUM(CASE WHEN status != 'Tersedia' THEN 1 ELSE 0 END) FROM barang");
            if (rs.next()) {
                lblTotal.setText(String.valueOf(rs.getInt(1)));
                lblDipinjam.setText(String.valueOf(rs.getInt(2)));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- UI HELPERS ---

    private JButton createNavBtn(String text, String target) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(230, 50));
        btn.setFont(new Font("Inter", Font.PLAIN, 14));
        btn.setForeground(new Color(148, 163, 184));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMargin(new Insets(0, 25, 0, 0));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> {
            if (target.equals("LOGOUT")) { this.dispose(); new LoginFrame().setVisible(true); }
            else { cardLayout.show(cardPanel, target); updateNavUI(btn); }
        });
        return btn;
    }

    private void updateNavUI(JButton activeBtn) {
        for (Component c : activeBtn.getParent().getComponents()) {
            if (c instanceof JButton) { c.setForeground(new Color(148, 163, 184)); ((JButton)c).setOpaque(false); }
        }
        activeBtn.setForeground(Color.WHITE); activeBtn.setOpaque(true);
        activeBtn.setBackground(new Color(255, 255, 255, 15));
        activeBtn.putClientProperty(FlatClientProperties.STYLE, "arc: 12");
    }

    private JPanel createStatCard(String title, JLabel val, Color c) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(c);
        card.putClientProperty(FlatClientProperties.STYLE, "arc: 35");
        card.setBorder(new EmptyBorder(30, 35, 30, 35));
        JLabel t = new JLabel(title);
        t.setForeground(new Color(255, 255, 255, 190));
        t.setFont(new Font("Inter", Font.BOLD, 13));
        val.setForeground(Color.WHITE); val.setFont(new Font("Inter", Font.BOLD, 48));
        card.add(t, BorderLayout.NORTH); card.add(val, BorderLayout.CENTER);
        return card;
    }
}