package view;

import com.formdev.flatlaf.FlatClientProperties;
import config.Config;
import dao.InventoryDAO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainFrame extends JFrame {
    private int currentAdminId;
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // Models & Tables
    private DefaultTableModel modelBarang, modelPeminjam, modelHistory;
    private JTable tableBarang, tablePeminjam, tableHistory;
    private JLabel lblTotal, lblDipinjam;
    private JComboBox<String> cbBarang;
    private JTextField txtIdPeminjam, txtSearchBarang, txtSearchPeminjam;
    private JSpinner spinnerTgl;

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
        refreshAllData();
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
        sidebar.add(createNavBtn("Inventory Assets", "BARANG"));
        sidebar.add(createNavBtn("User Management", "PEMINJAM"));
        sidebar.add(createNavBtn("Activity Logs", "HISTORY"));

        sidebar.add(Box.createVerticalStrut(250));
        sidebar.add(createNavBtn("Sign Out", "LOGOUT"));

        add(sidebar, BorderLayout.WEST);

        // --- CONTENT AREA ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG_SOFT);

        cardPanel.add(createDashboardPage(), "DASHBOARD");
        cardPanel.add(createBarangPage(), "BARANG");
        cardPanel.add(createPeminjamPage(), "PEMINJAM");
        cardPanel.add(createHistoryPage(), "HISTORY");

        add(cardPanel, BorderLayout.CENTER);
    }

    // --- LOGIC HANDLERS ---

    private void handleReturnAsset() {
        int row = tableHistory.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a log entry to return.");
            return;
        }

        // Ambil ID Peminjaman (Kolom 0) dan Lisensi (Kolom 5)
        String loanId = tableHistory.getValueAt(row, 0).toString();
        String licenseId = tableHistory.getValueAt(row, 5).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Mark asset " + licenseId + " as returned and clear from logs?",
                "Process Return", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Config.configDB()) {
                conn.setAutoCommit(false); // Start Transaction

                try {
                    // 1. Update status barang
                    PreparedStatement psBarang = conn.prepareStatement("UPDATE barang SET status = 'Tersedia' WHERE lisensi = ?");
                    psBarang.setString(1, licenseId);
                    psBarang.executeUpdate();

                    // 2. Hapus dari log peminjaman (agar hilang dari tabel history)
                    PreparedStatement psLoan = conn.prepareStatement("DELETE FROM peminjaman WHERE peminjaman_id = ?");
                    psLoan.setString(1, loanId);
                    psLoan.executeUpdate();

                    conn.commit(); // Save changes
                    JOptionPane.showMessageDialog(this, "Asset successfully returned and log cleared.");
                    refreshAllData();
                } catch (SQLException ex) {
                    conn.rollback(); // Undo if error
                    throw ex;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error returning asset: " + e.getMessage());
            }
        }
    }

    private void handleLoan() {
        try {
            if (cbBarang.getSelectedItem() == null || txtIdPeminjam.getText().isEmpty()) {
                throw new Exception("Please select an available asset and enter Borrower ID.");
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String tgl = sdf.format(spinnerTgl.getValue());
            new InventoryDAO().simpanTransaksi(
                    cbBarang.getSelectedItem().toString(),
                    Integer.parseInt(txtIdPeminjam.getText()),
                    currentAdminId, tgl
            );
            JOptionPane.showMessageDialog(this, "Loan executed successfully!");
            refreshAllData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Transaction Failed: " + e.getMessage());
        }
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

    private JPanel createBarangPage() {
        JPanel p = new JPanel(new BorderLayout(25, 25));
        p.setBackground(BG_SOFT);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        txtSearchBarang = new JTextField();
        txtSearchBarang.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search assets...");
        txtSearchBarang.setPreferredSize(new Dimension(400, 45));
        txtSearchBarang.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { refreshBarangData(txtSearchBarang.getText()); }
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(createActionBtn("Add Asset", SUCCESS_COLOR, e -> handleAddBarang()));
        actions.add(createActionBtn("Delete", DANGER_COLOR, e -> handleDelete("barang", tableBarang)));
        header.add(txtSearchBarang, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        modelBarang = createNonEditableModel(new String[]{"License ID", "Asset Name", "Category", "Status"});
        tableBarang = createModernTable(modelBarang);
        p.add(header, BorderLayout.NORTH);
        p.add(new JScrollPane(tableBarang), BorderLayout.CENTER);
        p.add(createLoanSidebar(), BorderLayout.EAST);
        return p;
    }

    private JPanel createPeminjamPage() {
        JPanel p = new JPanel(new BorderLayout(25, 25));
        p.setBackground(BG_SOFT);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        txtSearchPeminjam = new JTextField();
        txtSearchPeminjam.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "Search members...");
        txtSearchPeminjam.setPreferredSize(new Dimension(400, 45));
        txtSearchPeminjam.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) { refreshPeminjamData(txtSearchPeminjam.getText()); }
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);
        actions.add(createActionBtn("Add Member", SUCCESS_COLOR, e -> handleAddPeminjam()));
        actions.add(createActionBtn("Delete", DANGER_COLOR, e -> handleDelete("peminjam", tablePeminjam)));
        header.add(txtSearchPeminjam, BorderLayout.WEST);
        header.add(actions, BorderLayout.EAST);
        modelPeminjam = createNonEditableModel(new String[]{"User ID", "Full Name", "Contact"});
        tablePeminjam = createModernTable(modelPeminjam);
        p.add(header, BorderLayout.NORTH);
        p.add(new JScrollPane(tablePeminjam), BorderLayout.CENTER);
        return p;
    }

    private JPanel createHistoryPage() {
        JPanel p = new JPanel(new BorderLayout(25, 25));
        p.setBackground(BG_SOFT);
        p.setBorder(new EmptyBorder(30, 30, 30, 30));
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel("Activity Logs (Active Loans)");
        title.setFont(new Font("Inter", Font.BOLD, 22));
        JButton btnReturn = createActionBtn("Mark as Returned", ACCENT_COLOR, e -> handleReturnAsset());
        header.add(title, BorderLayout.WEST);
        header.add(btnReturn, BorderLayout.EAST);
        modelHistory = createNonEditableModel(new String[]{"ID", "Date", "Due Date", "Asset Name", "Borrower", "License ID"});
        tableHistory = createModernTable(modelHistory);
        p.add(header, BorderLayout.NORTH);
        p.add(new JScrollPane(tableHistory), BorderLayout.CENTER);
        return p;
    }

    // --- REFRESH DATA METHODS ---

    private void refreshHistoryData() {
        modelHistory.setRowCount(0);
        String sql = "SELECT p.peminjaman_id, p.tanggal_pinjam, p.tanggal_tenggat, b.nama_barang, u.nama_peminjam, b.lisensi " +
                "FROM peminjaman p " +
                "JOIN barang b ON p.lisensi = b.lisensi " +
                "JOIN peminjam u ON p.peminjam_id = u.peminjam_id " +
                "ORDER BY p.peminjaman_id DESC";
        try (Connection conn = Config.configDB(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                modelHistory.addRow(new Object[]{
                        rs.getInt(1), rs.getDate(2), rs.getDate(3), rs.getString(4), rs.getString(5), rs.getString(6)
                });
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refreshBarangData(String query) {
        modelBarang.setRowCount(0); cbBarang.removeAllItems();
        try (Connection conn = Config.configDB()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM barang WHERE nama_barang LIKE ? OR lisensi LIKE ?");
            ps.setString(1, "%"+query+"%"); ps.setString(2, "%"+query+"%");
            ResultSet rs = ps.executeQuery();
            int total = 0, pjm = 0;
            while (rs.next()) {
                String st = rs.getString("status");
                modelBarang.addRow(new Object[]{rs.getString(1), rs.getString(2), rs.getString(3), st});
                if (st.equals("Tersedia")) cbBarang.addItem(rs.getString(1)); else pjm++;
                total++;
            }
            lblTotal.setText(String.valueOf(total)); lblDipinjam.setText(String.valueOf(pjm));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refreshPeminjamData(String query) {
        modelPeminjam.setRowCount(0);
        try (Connection conn = Config.configDB()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM peminjam WHERE nama_peminjam LIKE ?");
            ps.setString(1, "%"+query+"%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) modelPeminjam.addRow(new Object[]{rs.getInt(1), rs.getString(2), rs.getString(3)});
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void refreshAllData() {
        refreshBarangData(""); refreshPeminjamData(""); refreshHistoryData();
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

    private JButton createActionBtn(String text, Color bg, ActionListener al) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Inter", Font.BOLD, 13));
        btn.setForeground(Color.WHITE); btn.setBackground(bg);
        btn.addActionListener(al);
        btn.putClientProperty(FlatClientProperties.STYLE, "arc: 12; borderWidth: 0; focusWidth: 0");
        return btn;
    }

    private JTable createModernTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(55);
        table.setShowGrid(false);
        table.getTableHeader().setPreferredSize(new Dimension(0, 50));
        table.getTableHeader().setFont(new Font("Inter", Font.BOLD, 13));
        table.getTableHeader().setBackground(Color.WHITE);
        table.putClientProperty(FlatClientProperties.STYLE, "arc: 20");
        return table;
    }

    private DefaultTableModel createNonEditableModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
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

    private JPanel createLoanSidebar() {
        JPanel f = new JPanel(new GridBagLayout());
        f.setPreferredSize(new Dimension(320, 0));
        f.setBackground(Color.WHITE);
        f.putClientProperty(FlatClientProperties.STYLE, "arc: 40");
        f.setBorder(new EmptyBorder(30, 25, 30, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.weightx = 1;
        JLabel title = new JLabel("Quick Loan");
        title.setFont(new Font("Inter", Font.BOLD, 22));
        gbc.insets = new Insets(0,0,25,0); f.add(title, gbc);
        cbBarang = new JComboBox<>();
        txtIdPeminjam = new JTextField();
        spinnerTgl = new JSpinner(new SpinnerDateModel(new Date(), null, null, Calendar.DAY_OF_MONTH));
        spinnerTgl.setEditor(new JSpinner.DateEditor(spinnerTgl, "yyyy-MM-dd"));
        addLabeledInput(f, "Available Asset", cbBarang, gbc, 1);
        addLabeledInput(f, "Borrower ID", txtIdPeminjam, gbc, 3);
        addLabeledInput(f, "Due Date", spinnerTgl, gbc, 5);
        JButton btnSave = createActionBtn("EXECUTE LOAN", ACCENT_COLOR, e -> handleLoan());
        btnSave.setPreferredSize(new Dimension(0, 50));
        gbc.gridy = 7; gbc.insets = new Insets(20, 0, 0, 0);
        f.add(btnSave, gbc);
        return f;
    }

    private void addLabeledInput(JPanel p, String label, JComponent c, GridBagConstraints gbc, int y) {
        gbc.gridy = y; gbc.insets = new Insets(0,0,8,0);
        JLabel lbl = new JLabel(label); lbl.setFont(new Font("Inter", Font.BOLD, 12));
        p.add(lbl, gbc);
        gbc.gridy = y+1; gbc.insets = new Insets(0,0,20,0);
        c.putClientProperty(FlatClientProperties.STYLE, "arc: 12; height: 42");
        p.add(c, gbc);
    }

    private void handleAddBarang() {
        JTextField name = new JTextField(); JTextField lic = new JTextField(); JTextField cat = new JTextField();
        Object[] msg = {"Name:", name, "License ID:", lic, "Category:", cat};
        if (JOptionPane.showConfirmDialog(this, msg, "Add New Asset", JOptionPane.OK_CANCEL_OPTION) == 0) {
            try (Connection conn = Config.configDB()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO barang VALUES (?,?,?, 'Tersedia')");
                ps.setString(1, lic.getText()); ps.setString(2, name.getText()); ps.setString(3, cat.getText());
                ps.executeUpdate(); refreshAllData();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error adding asset."); }
        }
    }

    private void handleAddPeminjam() {
        JTextField name = new JTextField(); JTextField telp = new JTextField();
        Object[] msg = {"Full Name:", name, "Contact Number:", telp};
        if (JOptionPane.showConfirmDialog(this, msg, "Add New Member", JOptionPane.OK_CANCEL_OPTION) == 0) {
            try (Connection conn = Config.configDB()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO peminjam (nama_peminjam, no_telepon) VALUES (?,?)");
                ps.setString(1, name.getText()); ps.setString(2, telp.getText());
                ps.executeUpdate(); refreshAllData();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error adding member."); }
        }
    }

    private void handleDelete(String table, JTable jTable) {
        int row = jTable.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Select a row first!"); return; }
        String id = jTable.getValueAt(row, 0).toString();
        if (JOptionPane.showConfirmDialog(this, "Delete ID: " + id + "?") == 0) {
            try (Connection conn = Config.configDB()) {
                String pk = table.equals("barang") ? "lisensi" : "peminjam_id";
                conn.createStatement().executeUpdate("DELETE FROM " + table + " WHERE " + pk + " = '" + id + "'");
                refreshAllData();
            } catch (Exception e) { JOptionPane.showMessageDialog(this, "Error deleting data."); }
        }
    }
}