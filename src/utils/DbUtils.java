package utils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Vector;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


public class DbUtils {
    public static String insertRow(Product product, String dbName) {
        String sql;
        if (product.getId().equals(""))
            sql = "INSERT INTO product_sales (Date, Region, Product, Qty, Cost, Amt, Tax, Total) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        else {
            sql = "INSERT INTO product_sales (id, Date, Region, Product, Qty, Cost, Amt, Tax, Total) VALUES (?,? ,?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int i =1;
            if(!product.getId().equals(""))
                pstmt.setString(i++, product.getId());
            pstmt.setString(i++, product.getDate());
            pstmt.setString(i++, product.getRegion());
            pstmt.setString(i++, product.getProduct());
            pstmt.setInt(i++, product.getQty());
            pstmt.setDouble(i++, product.getCost());
            pstmt.setDouble(i++, product.getAmt());
            pstmt.setDouble(i++, product.getTax());
            pstmt.setDouble(i++, product.getTotal());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            int id = -1;
            if (rs.next()) {
                id = rs.getInt(1); // Get the id of the last inserted row
            }
            return Integer.toString(id);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static void updateRow(Product product, String dbName) {
        String sql = "UPDATE product_sales SET Date=?, Region=?, Product=?, Qty=?, Cost=?, Amt=?, Tax=?, Total=? WHERE ID=?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getDate());
            pstmt.setString(2, product.getRegion());
            pstmt.setString(3, product.getProduct());
            pstmt.setInt(4, product.getQty());
            pstmt.setDouble(5, product.getCost());
            pstmt.setDouble(6, product.getAmt());
            pstmt.setDouble(7, product.getTax());
            pstmt.setDouble(8, product.getTotal());
            pstmt.setString(9, product.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void insertProducts(List<Product> productList, String dbName, int i) {
        try {
            for (Product product : productList) {
                product.setId("BO" + i + product.getId());
                insertRow(product, dbName);
            }

        } catch (Exception e) {
            System.out.println("Error inserting products: " + e.getMessage());
        }
    }

    public static List<Product> getAllProducts(String dbName) {
        List<Product> productList = new ArrayList<>();

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);

            String sql = "SELECT * FROM product_sales";
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String id = rs.getString("id");
                String date = rs.getString("date");
                String region = rs.getString("region");
                String product = rs.getString("product");
                int qty = rs.getInt("qty");
                double cost = rs.getDouble("cost");
                double amt = rs.getDouble("amt");
                double tax = rs.getDouble("tax");
                double total = rs.getDouble("total");

                Product p = new Product(id, date, region, product, qty, cost, amt, tax, total);
                productList.add(p);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error selecting products: " + e.getMessage());
        }

        return productList;
    }


    public static List<Product> getMissingProducts(String dbName, String att, int i) {
        List<Product> productList = new ArrayList<>();

        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);

            String sql = "SELECT * FROM product_sales where " + att + "= 1";
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String id = rs.getString("id");
                String date = rs.getString("date");
                String region = rs.getString("region");
                String product = rs.getString("product");
                int qty = rs.getInt("qty");
                double cost = rs.getDouble("cost");
                double amt = rs.getDouble("amt");
                double tax = rs.getDouble("tax");
                double total = rs.getDouble("total");

                Product p = new Product("BO"+i+""+id, date, region, product, qty, cost, amt, tax, total);
                productList.add(p);
            }

            sql = "UPDATE product_sales set " + att +"= 0";
            stmt.executeUpdate(sql);

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println("Error selecting products: " + e.getMessage());
        }

        return productList;
    }


    public static void deleteRow(String id, String dbName) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);

            String sql = "DELETE FROM product_sales WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.executeUpdate();

            pstmt.close();
            conn.close();

            System.out.println("Row deleted successfully.");
        } catch (SQLException e) {
            System.out.println("Error deleting row: " + e.getMessage());
        }
    }

    public static void updateBORow(Product product, String dbName) {
        String sql = "UPDATE product_sales SET Date=?, Region=?, Product=?, Qty=?, Cost=?, Amt=?, Tax=?, Total=?, updated=1 WHERE ID=?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, product.getDate());
            pstmt.setString(2, product.getRegion());
            pstmt.setString(3, product.getProduct());
            pstmt.setInt(4, product.getQty());
            pstmt.setDouble(5, product.getCost());
            pstmt.setDouble(6, product.getAmt());
            pstmt.setDouble(7, product.getTax());
            pstmt.setDouble(8, product.getTotal());
            pstmt.setString(9, product.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void deleteBORow(String id, String dbName) {
        String sql = "UPDATE product_sales SET deleted=1 WHERE ID=?";

        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static DefaultTableModel resultSetToTableModel(ResultSet rs) {
        try {

            ResultSetMetaData metaData = rs.getMetaData();
            int numberOfColumns = metaData.getColumnCount();
            Vector columnNames = new Vector();

            for (int column = 0; column < numberOfColumns; column++) {
                columnNames.addElement(metaData.getColumnLabel(column + 1));
            }

            Vector rows = new Vector();
            while (rs.next()) {

                Vector newRow = new Vector();
                for (int i = 1; i <= numberOfColumns; i++) {
                    newRow.addElement(rs.getObject(i));
                }

                rows.addElement(newRow);

            }
            return new DefaultTableModel(rows, columnNames);
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }

}
