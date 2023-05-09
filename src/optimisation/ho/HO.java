package optimisation.ho;

import com.rabbitmq.client.*;
import utils.DbUtils;
import utils.Product;
import utils.ProductSalesGUI;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class HO {
    private final static String[] EXCHANGES = {"bo_1", "bo_2"};
    private final static String DB_NAME = "product_sales.db";
    public static int[] BO = {1, 2};

    public HO() throws IOException, TimeoutException {
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
                Product p = Product.deserialize(message);
                DbUtils.insertRow(p, DB_NAME);
            } else if(routingKey.equals("update")){
                Product p = Product.deserialize(message);
                DbUtils.updateRow(p, DB_NAME);
            } else if (routingKey.equals("delete")){
                DbUtils.deleteRow(message, DB_NAME);
            } else {
                System.out.println("routing_key ERROR");
            }

        };

        for (int i: BO)
            channel.basicConsume(queue[i-1], true, deliverCallback, consumerTag -> {});
    }

    public static void main(String[] args) {
        new ProductSalesGUI(DB_NAME);
        try {
            new HO();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
    }
}
