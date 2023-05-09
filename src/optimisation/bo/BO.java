package optimisation.bo;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.nio.charset.StandardCharsets;

import com.rabbitmq.client.*;
import utils.DbUtils;
import utils.Product;

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

    private void alertHO(Product p, String routing_key) throws Exception{

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            String message= p.serialize();

            channel.basicPublish(EXCHANGE_NAME, routing_key, MessageProperties.PERSISTENT_BASIC, message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '"+ routing_key + "message: '" + message+"'");

    }

    private void alertHODelete(String p) throws Exception{

            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

            channel.basicPublish(EXCHANGE_NAME, "delete", MessageProperties.PERSISTENT_BASIC, p.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '"+ "delete" + "message: '" + p+"'");

    }


    public BO(String db, int n) {
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
        dateField = new JTextField(20);
        formPanel.add(dateLabel);
        formPanel.add(dateField);

        JLabel regionLabel = new JLabel("Region:");
        regionField = new JTextField(20);
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
        } catch (Exception e){
            e.printStackTrace();
        }

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String id =idField.getText().trim();

                String date = dateField.getText().trim();
                String region = regionField.getText().trim();
                String product = productField.getText().trim();
                int qty = Integer.parseInt(qtyField.getText().trim());
                double cost = Double.parseDouble(costField.getText().trim());
                double amt = Double.parseDouble(amtField.getText().trim());
                double tax = Double.parseDouble(taxField.getText().trim());
                double total = Double.parseDouble(totalField.getText().trim());
                String routing_key;
                Product p = new Product(id, date, region, product, qty, cost, amt, tax, total);

                if (id.isEmpty()) {
                    String x = DbUtils.insertRow(p, dbName);
                    p.setId("BO" + i + x);
                    routing_key = "insert";
                } else {
                    DbUtils.updateRow(p, dbName);
                    p.setId("BO" + i + id);
                    routing_key = "update";
                }
                try {
                    alertHO(p, routing_key);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String id =idField.getText().trim();

                String routing_key;
                if (id.isEmpty()) {
                    JOptionPane.showMessageDialog(deleteButton, "No id provided");
                } else {
                    DbUtils.deleteRow(id, dbName);
                    id = "BO" + i + id;
                    try {
                        alertHODelete(id);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }
        });

        formPanel.add(saveButton);
        formPanel.add(deleteButton);
        frame.add(formPanel);
        frame.pack();
        frame.setTitle(this.dbName);
        frame.setVisible(true);
    }
}
