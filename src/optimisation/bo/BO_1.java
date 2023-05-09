package optimisation.bo;

import utils.ProductSalesGUI;

public class BO_1 {
    public static String DB_NAME="BO_1_sales.db";

    public static void main(String[] args) {
        new ProductSalesGUI(DB_NAME);
        new BO(DB_NAME, 1);
    }
}
