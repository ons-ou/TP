package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import java.util.Timer;
import java.util.TimerTask;

public class ProductSalesGUI {
    private JTable table;
    private String dbName;

    public ProductSalesGUI(String db) {
        this.dbName = db;
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame("Product Sales Table");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(scrollPane);
        frame.pack();
        frame.setTitle(this.dbName);
        frame.setVisible(true);
        Timer timer = new Timer();
        timer.schedule(new RefreshTask(), 0, 20000);
    }

    private class RefreshTask extends TimerTask {
        public void run() {
            Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            System.out.println("Updating the database...");
            try {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:"+dbName);
                stmt = conn.createStatement();
                rs = stmt.executeQuery("SELECT * FROM product_sales");
                table.setModel(DbUtils.resultSetToTableModel(rs));

                conn.close();
                stmt.close();
                rs.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

}
