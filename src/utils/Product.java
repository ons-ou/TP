package utils;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

public class Product implements Serializable {
    private String id;
    private String date;
    private String region;
    private String product;
    private int qty;
    private double cost;
    private double amt;
    private double tax;
    private double total;

    public Product(String id, String date, String region, String product, int qty, double cost, double amt, double tax, double total) {
        this.id = id;
        this.date = date;
        this.region = region;
        this.product = product;
        this.qty = qty;
        this.cost = cost;
        this.amt = amt;
        this.tax = tax;
        this.total = total;
    }

    public String serialize() {
        String serialized = id + "," + date + "," + region + "," + product + "," + qty + "," + cost + "," + amt + "," + tax + "," + total;
        return serialized;
    }

    public static Product deserialize(String serialized) {
        String[] fields = serialized.split(",");
        String id = fields[0];
        String date = fields[1];
        String region = fields[2];
        String product = fields[3];
        int qty = Integer.parseInt(fields[4]);
        double cost = Double.parseDouble(fields[5]);
        double amt = Double.parseDouble(fields[6]);
        double tax = Double.parseDouble(fields[7]);
        double total = Double.parseDouble(fields[8]);
        return new Product(id, date, region, product, qty, cost, amt, tax, total);
    }

    public static String serializeProductList(List<Product> productList) {
        StringBuilder serialized = new StringBuilder();
        for (Product p : productList) {
            serialized.append(p.serialize()).append("\n");
        }
        return serialized.toString();
    }

    public static List<Product> deserializeProductList(String serializedProductList) {
        List<Product> productList = new ArrayList<>();
        String[] lines = serializedProductList.split("\n");
        for (String line : lines) {
            Product p = Product.deserialize(line);
            productList.add(p);
        }
        return productList;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public String getRegion() {
        return region;
    }

    public String getProduct() {
        return product;
    }

    public int getQty() {
        return qty;
    }

    public double getCost() {
        return cost;
    }

    public double getAmt() {
        return amt;
    }

    public double getTax() {
        return tax;
    }

    public double getTotal() {
        return total;
    }

}
