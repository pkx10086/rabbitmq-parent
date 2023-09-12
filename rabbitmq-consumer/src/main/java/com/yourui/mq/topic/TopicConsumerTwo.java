package com.yourui.mq.topic;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.yourui.mq.util.RabbitMqUtil;

/**
 * @author : pankx
 * @description: TOPIC 通配符模式-消费者2，消费routingKey=*.*.rabbit,lazy.# 消息
 * @date: 2023/9/5  19:43
 */
public class TopicConsumerTwo {

    private static final String EXCHANGE_NAME = "topic_logs";
    /**
     * 队列名称
     */
    static final String TOPIC_QUEUE_TWO = "topic_queue_two";

    public static void main(String[] argv) throws Exception {
        //创建链接
        Connection connection = RabbitMqUtil.newConnection();
        //创建通道
        Channel channel = connection.createChannel();
        /** * 声明交换机
         * 参数1：交换机名称
         * 参数2：交换机类型，fanout、topic、direct、headers
         * */
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        /** 声明（创建）队列
         *  参数1：队列名称
         *  参数2：是否定义持久化队列
         *  参数3：是否独占本次连接
         *  参数4：是否在不使用的时候自动删除队列
         *  参数5：队列其它参数 */
        channel.queueDeclare(TOPIC_QUEUE_TWO, true, false, false, null);
        //队列绑定交换机
        channel.queueBind(TOPIC_QUEUE_TWO, EXCHANGE_NAME, "*.*.rabbit");
        channel.queueBind(TOPIC_QUEUE_TWO, EXCHANGE_NAME, "lazy.#");


        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        /**接收队列中数据的方法
         回调方法,当收到消息后,会自动执行该方法
         1. consumerTag：标识
         2. envelope：获取一些信息,交换机,路由key...
         3. properties：配置信息
         4. body：数据
         */
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + delivery.getEnvelope().getRoutingKey() + "':'" + message + "'");
        };

        /** * 参数1：队列名称
         * 参数2：是否自动确认，设置为true为表示消息接收到自动向mq回复接收到了，mq接收到回复会删除消息，设置为false则需要手动确认
         * 参数3：消息接收到后回调 */
        channel.basicConsume(TOPIC_QUEUE_TWO, true, deliverCallback, consumerTag -> {
        });
    }
}
