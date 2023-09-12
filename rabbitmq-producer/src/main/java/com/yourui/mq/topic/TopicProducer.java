package com.yourui.mq.topic;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yourui.mq.util.RabbitMqUtil;

import java.text.MessageFormat;

/**
 * @author : pankx
 * @description: Topic 通配符模式
 * @date: 2023/9/5  19:09
 */
public class TopicProducer {

    private static final String EXCHANGE_NAME = "topic_logs";
    /**
     * 队列名称
     */
    static final String TOPIC_QUEUE_ONE = "topic_queue_one";
    /**
     * 队列名称
     */
    static final String TOPIC_QUEUE_TWO = "topic_queue_two";

    public static void main(String[] argv) throws Exception {

        try (Connection connection = RabbitMqUtil.newConnection();
             Channel channel = connection.createChannel()) {
            /** * 声明交换机
             * 参数1：交换机名称
             * 参数2：交换机类型，fanout、topic、direct、headers
             * */
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
            /**声明（创建）队列
             * 参数1：队列名称
             * 参数2：是否定义持久化队列
             * 参数3：是否独占本次连接
             * 参数4：是否在不使用的时候自动删除队列
             * 参数5：队列其它参数
             **/
            channel.queueDeclare(TOPIC_QUEUE_ONE, true, false, false, null);
            channel.queueDeclare(TOPIC_QUEUE_TWO, true, false, false, null);

            /**队列绑定交换机
             * 参数1：队列名称
             * 参数2：交换机名称
             * 参数3：路由key，RoutingKey
             * */
            channel.queueBind(TOPIC_QUEUE_ONE, EXCHANGE_NAME, "*.orange.*");

            /**将 info、warning、debug 路由key将到队列DIRECT_QUEUE_NOT_ERROR和交换机DIRECT_QUEUE_ERROR进行绑定
             * */
            channel.queueBind(TOPIC_QUEUE_TWO, EXCHANGE_NAME, "*.*.rabbit");
            channel.queueBind(TOPIC_QUEUE_TWO, EXCHANGE_NAME, "lazy.#");


            while (true) {
                String routingkey = randomRoutingKey();
                String message = MessageFormat.format("[{0}]日志信息", routingkey);
                /** * 参数1：交换机名称，如果没有指定则使用默认Default Exchage
                 * * 参数2：路由key,简单模式可以传递队列名称
                 *  参数3：消息其它属性
                 *  参数4：消息内容
                 * */
                channel.basicPublish(EXCHANGE_NAME, routingkey, null, message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + routingkey + "':'" + message + "'");
                Thread.sleep(2000);
            }
        }
    }


    /**
     * @return {@link String}
     * 随机生成日志柔婷key
     */
    public static String randomRoutingKey() {
        int num = (int) (Math.random() * 5);
        String routingkey;
        switch (num) {
            case 1:
                routingkey = "quick.orange.rabbit";
                break;
            case 2:
                routingkey = "lazy.orange.elephant";
                break;
            case 3:
                routingkey = "quick.orange.fox";
                break;
            case 4:
                routingkey = "lazy.brown.fox";
                break;
            default:
                routingkey="quick.brown.fox";
        }

        return routingkey;
    }


}