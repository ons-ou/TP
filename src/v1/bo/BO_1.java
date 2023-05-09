package v1.bo;

import utils.ProductSalesGUI;

import java.text.ParseException;

public class BO_1 {
    public static String DB_NAME="BO_1_sales.db";

    public static void main(String[] args) {
        new ProductSalesGUI(DB_NAME);
        try {
            new BO(DB_NAME, 1);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
