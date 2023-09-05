package com.yourui.mq.fanout;


import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.yourui.mq.util.RabbitMqUtil;

/**
 * @author : pankx
 * @description: 发布订阅模式-扇出消费者-采用自定义队列
 * @date: 2023/9/4  16:51
 */
public class FanoutCousumer3 {

    private static final String EXCHANGE_NAME = "fanout_logs";
    private static final String FANOUT_QUEUE_1 = "fanout_queue_1";
    public static void main(String[] argv) throws Exception {
        Connection connection = RabbitMqUtil.newConnection();
        Channel channel = connection.createChannel();
        //声明交换机(交换机名称，交换机类型)
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        //声明队列
        channel.queueDeclare(FANOUT_QUEUE_1,true,false,false,null);
        //将队列绑定到交换机上
        channel.queueBind(FANOUT_QUEUE_1, EXCHANGE_NAME, "");

        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
        };
        channel.basicConsume(FANOUT_QUEUE_1, true, deliverCallback, consumerTag -> { });

    }
}