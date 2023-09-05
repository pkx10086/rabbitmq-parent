package com.yourui.mq.fanout;


import com.rabbitmq.client.*;
import com.yourui.mq.util.RabbitMqUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author : pankx
 * @description: 发布订阅模式-扇出消费者-采用随机队列
 * @date: 2023/9/4  16:51
 */
public class FanoutCousumer {

    private static final String EXCHANGE_NAME = "fanout_logs";

    public static void main(String[] argv) throws Exception {
        //创建链接
        Connection connection = RabbitMqUtil.newConnection();
        //创建通道
        Channel channel = connection.createChannel();
        //声明交换机(交换机名称，交换机类型)
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
        //生成随机队列
        String queueName = channel.queueDeclare().getQueue();
        //打印随机队列
        System.out.println("[log] Existing queues "+queueName+"");
        //将队列绑定到交换机上
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        //打印等待消息的日志
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

        /**接收队列中数据的方法
         回调方法,当收到消息后,会自动执行该方法
         1. consumerTag：标识
         2. envelope：获取一些信息,交换机,路由key...
         3. properties：配置信息
         4. body：数据
         */
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [x] Received '" + message + "'");
        };
        /**参数：
         1. queue：队列名称
         2. autoAck：是否自动确认 ,类似咱们发短信,发送成功会收到一个确认消息(如果自动签收，会删除队列的该条消息)
         3. DeliverCallback：回调对象
         4、CancelCallback :中断正在执行的调度任务
         */

        //消费者类似一个监听程序,主要是用来监听消息
        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> { });
    }
}