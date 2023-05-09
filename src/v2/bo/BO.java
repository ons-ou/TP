package v2.bo;

import com.rabbitmq.client.*;
import utils.DbUtils;
import utils.Product;
import utils.ProductSalesGUI;

import javax.swing.*;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BO {
    private int i;
    private Connection connection;
    private Channel channel;
    private ConnectionFactory factory;
    private String EXCHANGE_NAME = "bo_";
    private String dbName;
    private JTextField idField;
    private JTextField dateField;
    private JTextField regionField;
    private JTextField productField;
    private JTextField qtyField;
    private JTextField costField;
    private JTextField amtField;
    private JTextField taxField;
    private JTextField totalField;

    private void alertHO() throws Exception{
        System.out.println("Alerting HO of changes....");
        List<Product> p = DbUtils.getMissingProducts(dbName, "new", i);
        String message= Product.serializeProductList(p);

        if (!message.isEmpty()) {
            channel.basicPublish(EXCHANGE_NAME, "insert", MessageProperties.PERSISTENT_BASIC, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent ' new Products: '" + message + "'");
        }

        p = DbUtils.getMissingProducts(dbName, "updated", i);
        message = Product.serializeProductList(p);
        if(!message.isEmpty()) {
            channel.basicPublish(EXCHANGE_NAME, "update", MessageProperties.PERSISTENT_BASIC, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent ' updated Products: '" + message + "'");
        }

        p = DbUtils.getMissingProducts(dbName, "deleted", i);
        message = "";
        for (Product x: p)
            message+= x.getId() + ",";
        if (!message.isEmpty()) {
            channel.basicPublish(EXCHANGE_NAME, "delete", MessageProperties.PERSISTENT_BASIC, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent ' deleted Products: '" + message + "'");
            for (Product i : p)
                DbUtils.deleteRow(Character.toString(i.getId().charAt(i.getId().length()-1)), dbName);
        }
    }

    private class RefreshTask extends TimerTask {
        public void run() {
            try {
                alertHO();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public BO(String db, int n) throws ParseException {
        i = n;
        EXCHANGE_NAME+=i;
        dbName = db;
        JFrame frame = new JFrame("Product Sales Table");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(10, 2));

        JLabel idLabel = new JLabel("ID:");
        idField = new JTextField(20);
        formPanel.add(idLabel);
        formPanel.add(idField);

        JLabel dateLabel = new JLabel("Date:");
        MaskFormatter dateFormatter = new MaskFormatter("##/##/####");
        JFormattedTextField dateField = new JFormattedTextField(dateFormatter);
        formPanel.add(dateLabel);
        formPanel.add(dateField);

        JLabel regionLabel = new JLabel("Region:");
        String[] regions = {"East", "North", "West", "South"};
        JComboBox<String> regionField = new JComboBox<>(regions);
        formPanel.add(regionLabel);
        formPanel.add(regionField);

        JLabel productLabel = new JLabel("Product:");
        productField = new JTextField(20);
        formPanel.add(productLabel);
        formPanel.add(productField);

        JLabel qtyLabel = new JLabel("Qty:");
        qtyField = new JTextField(20);
        formPanel.add(qtyLabel);
        formPanel.add(qtyField);

        JLabel costLabel = new JLabel("Cost:");
        costField = new JTextField(20);
        formPanel.add(costLabel);
        formPanel.add(costField);

        JLabel amtLabel = new JLabel("Amt:");
        amtField = new JTextField(20);
        formPanel.add(amtLabel);
        formPanel.add(amtField);

        JLabel taxLabel = new JLabel("Tax:");
        taxField = new JTextField(20);
        formPanel.add(taxLabel);
        formPanel.add(taxField);

        JLabel totalLabel = new JLabel("Total:");
        totalField = new JTextField(20);
        formPanel.add(totalLabel);
        formPanel.add(totalField);

        factory = new ConnectionFactory();
        factory.setHost("localhost");
        try {
            connection = factory.newConnection();
            this.channel = connection.createChannel();
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
        } catch (Exception e){
            e.printStackTrace();
        }

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String id =idField.getText().trim();

                String date = dateField.getText().trim();
                String region = (String) regionField.getSelectedItem();                String product = productField.getText().trim();
                int qty = Integer.parseInt(qtyField.getText().trim());
                double cost = Double.parseDouble(costField.getText().trim());
                double amt = Double.parseDouble(amtField.getText().trim());
                double tax = Double.parseDouble(taxField.getText().trim());
                double total = Double.parseDouble(totalField.getText().trim());

                Product p = new Product(id, date, region, product, qty, cost, amt, tax, total);

                if (id.isEmpty()) {
                    String x = DbUtils.insertRow(p, dbName);
                } else {
                    DbUtils.updateBORow(p, dbName);
                }
            }
        });


        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String id =idField.getText().trim();
                if (id.isEmpty()) {
                    JOptionPane.showMessageDialog(deleteButton, "No id provided");
                } else {
                    DbUtils.deleteBORow(id, dbName);
                }

            }
        });

        formPanel.add(saveButton);
        formPanel.add(deleteButton);
        frame.add(formPanel);
        frame.pack();
        frame.setTitle(this.dbName);
        frame.setVisible(true);

        java.util.Timer timer = new Timer();
        timer.schedule(new RefreshTask(), 0, 20000);
    }
}
