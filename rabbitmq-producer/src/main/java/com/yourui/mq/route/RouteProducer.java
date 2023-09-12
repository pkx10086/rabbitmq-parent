package com.yourui.mq.route;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yourui.mq.util.RabbitMqUtil;

import java.text.MessageFormat;
import java.util.Random;

/**
 * @author : pankx
 * @description: 路由模式-
 * @date: 2023/9/5  19:09
 */
public class RouteProducer {

    private static final String EXCHANGE_NAME = "direct_exchange_logs";
    /**
     * 队列名称
     */
    static final String DIRECT_QUEUE_ERROR = "error_log_queue";
    /**
     * 队列名称
     */
    static final String DIRECT_QUEUE_NOT_ERROR = "no_error_log_queue";

    public static void main(String[] argv) throws Exception {

        try (Connection connection = RabbitMqUtil.newConnection();
             Channel channel = connection.createChannel()) {
            /** * 声明交换机
             * 参数1：交换机名称
             * 参数2：交换机类型，fanout、topic、direct、headers
             * */
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);
            /**声明（创建）队列
             * 参数1：队列名称
             * 参数2：是否定义持久化队列
             * 参数3：是否独占本次连接
             * 参数4：是否在不使用的时候自动删除队列
             * 参数5：队列其它参数
             **/
            channel.queueDeclare(DIRECT_QUEUE_ERROR, true, false, false, null);
            channel.queueDeclare(DIRECT_QUEUE_NOT_ERROR, true, false, false, null);

            /**队列绑定交换机
             * 参数1：队列名称
             * 参数2：交换机名称
             * 参数3：路由key，RoutingKey
             * */
            channel.queueBind(DIRECT_QUEUE_ERROR, EXCHANGE_NAME, "error");

            /**将 info、warning、debug 路由key将到队列DIRECT_QUEUE_NOT_ERROR和交换机DIRECT_QUEUE_ERROR进行绑定
             * */
            channel.queueBind(DIRECT_QUEUE_NOT_ERROR, EXCHANGE_NAME, "info");
            channel.queueBind(DIRECT_QUEUE_NOT_ERROR, EXCHANGE_NAME, "warning");
            channel.queueBind(DIRECT_QUEUE_NOT_ERROR, EXCHANGE_NAME, "debug");


            while (true) {
                String routingkey = randomRoutingKey();
                String message = MessageFormat.format("[{0}]日志信息:调用了方法，日志级别是", routingkey);
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
                routingkey = "info";
                break;
            case 2:
                routingkey = "warning";
                break;
            case 3:
                routingkey = "debug";
                break;
            case 4:
                routingkey = "error";
                break;
            default:
                routingkey="info";
        }

        return routingkey;
    }


}