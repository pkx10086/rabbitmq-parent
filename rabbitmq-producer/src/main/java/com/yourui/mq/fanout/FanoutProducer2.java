package com.yourui.mq.fanout;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yourui.mq.util.RabbitMqUtil;

import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

/**
 * @author : pankx
 * @description: 发布订阅模式-扇出交换机-采用随机队列
 * @date: 2023/9/4  16:49
 */
public class FanoutProducer2 {


    private static final String EXCHANGE_NAME = "fanout_logs";
    public static String FANOUT_QUEUE_1 = "fanout_queue_1";
    public static String FANOUT_QUEUE_2 = "fanout_queue_2";

    public static void main(String[] argv) throws Exception {
        try (Connection connection = RabbitMqUtil.newConnection();
             Channel channel = connection.createChannel()) {
            //声明交换机(交换机名称，交换机类型)
            channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            //声明队列
            channel.queueDeclare(FANOUT_QUEUE_1,true,false,false,null);
            channel.queueDeclare(FANOUT_QUEUE_2,true,false,false,null);
            //把交换机和队列进行绑定
            /**
             * 把交换机和队列进行绑定
             * queue      参数1：要绑定的队列的名称。这是一个字符串参数，指定了队列的名称
             * exchange    参数2：要绑定的交换机的名称。这是一个字符串参数，指定了要与队列绑定的交换机的名称。
             * routingKey  参数3：用于将消息路由到队列的路由键。这是一个字符串参数，
             * 用于指定将消息路由到绑定的队列的路由规则。路由键是由应用程序生成的字符串，
             * 它与交换机的配置相关联，以确定消息将发送到哪个队列。
             * 扇区类型的交换机设置routingkey是不起作用的，默认为空就行
             */
            channel.queueBind(FANOUT_QUEUE_1,EXCHANGE_NAME,"");
            channel.queueBind(FANOUT_QUEUE_2,EXCHANGE_NAME,"");

            int seq = 0;
            while (true) {
                String message = MessageFormat.format("第{0}发布订阅消息", seq);
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
                System.out.println(" [x] Sent '" + message + "'");
                seq++;
                //休眠1秒，每秒发送1条
                Thread.sleep(1000);
            }
        }
    }

}