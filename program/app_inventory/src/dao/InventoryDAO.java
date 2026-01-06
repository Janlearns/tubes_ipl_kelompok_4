package dao;

import config.Config;
import java.sql.*;

public class InventoryDAO {

    // FUNGSI UNTUK LOGIN & MENGAMBIL ID ADMIN (Penting untuk Session)
    public int getAdminId(String username, String password) {
        try (Connection conn = Config.configDB()) {
            String sql = "SELECT admin_id FROM admin WHERE username=? AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("admin_id"); // Mengembalikan ID Admin asli
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; // Mengembalikan -1 jika login gagal
    }

    // FUNGSI SIMPAN TRANSAKSI PEMINJAMAN
    public void simpanTransaksi(String lisensi, int idPeminjam, int idAdmin, String tglTenggat) throws Exception {
        Connection conn = Config.configDB();
        conn.setAutoCommit(false); // Memulai transaksi database

        try {
            // 1. Input ke tabel peminjaman
            String sqlPinjam = "INSERT INTO peminjaman (tanggal_pinjam, tanggal_tenggat, lisensi, peminjam_id, admin_id) VALUES (CURDATE(), ?, ?, ?, ?)";
            PreparedStatement psPinjam = conn.prepareStatement(sqlPinjam);
            psPinjam.setString(1, tglTenggat);
            psPinjam.setString(2, lisensi);
            psPinjam.setInt(3, idPeminjam);
            psPinjam.setInt(4, idAdmin);
            psPinjam.executeUpdate();

            // 2. Update status barang menjadi 'Dipinjam'
            String sqlUpdateBarang = "UPDATE barang SET status = 'Dipinjam' WHERE lisensi = ?";
            PreparedStatement psUpdate = conn.prepareStatement(sqlUpdateBarang);
            psUpdate.setString(1, lisensi);
            psUpdate.executeUpdate();

            conn.commit(); // Simpan perubahan secara permanen
        } catch (SQLException e) {
            conn.rollback(); // Batalkan jika ada yang error
            throw new Exception("Gagal simpan: " + e.getMessage());
        } finally {
            conn.close();
        }
    }
}