package v1.ho;

import com.rabbitmq.client.*;
import utils.DbUtils;
import utils.Product;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class HO {
    private final static String[] EXCHANGES = {"bo_1", "bo_2"};
    private final static String DB_NAME = "product_sales.db";
    public static int[] BO = {1, 2};
    private JTable table;


    public void updateTable(){
        java.sql.Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        System.out.println("Updating the database...");
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:"+DB_NAME);
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

    public HO() throws IOException, TimeoutException {
        table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);

        JFrame frame = new JFrame("Product Sales Table");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(scrollPane);
        JButton req = new JButton("Update Database");
        frame.add(req, BorderLayout.SOUTH);
        frame.pack();
        frame.setTitle(DB_NAME);
        frame.setVisible(true);
        updateTable();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        com.rabbitmq.client.Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        String[] queue = new String[BO.length];

        for (int i =0 ; i< BO.length; i++) {
            channel.exchangeDeclare(EXCHANGES[i], BuiltinExchangeType.DIRECT);
            queue[i] = channel.queueDeclare("bo_queue_" + BO[i], true, false, false, null).getQueue();
            channel.queueBind(queue[i], EXCHANGES[i], "insert");
            channel.queueBind(queue[i], EXCHANGES[i], "update");
            channel.queueBind(queue[i], EXCHANGES[i], "delete");
        }

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println("[*] Received Message : " + message);
            String routingKey = delivery.getEnvelope().getRoutingKey();
            if (routingKey.equals("insert")){
                List<Product> l = Product.deserializeProductList(message);
                for (Product p: l)
                    DbUtils.insertRow(p, DB_NAME);
            } else if(routingKey.equals("update")){
                List<Product> l = Product.deserializeProductList(message);
                for (Product p: l)
                    DbUtils.updateRow(p, DB_NAME);
            } else if (routingKey.equals("delete")){
                String[] l = message.split(",");
                for (String p:l)
                    DbUtils.deleteRow(p, DB_NAME);
            } else {
                System.out.println("routing_key ERROR");
            }

            updateTable();
        };

        for (int i: BO)
            channel.basicConsume(queue[i-1], true, deliverCallback, consumerTag -> {});

        req.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    channel.basicPublish("request", "", null, "".getBytes(StandardCharsets.UTF_8));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        try {
            new HO();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
