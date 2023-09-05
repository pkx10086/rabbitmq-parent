package com.yourui.mq.fanout;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yourui.mq.util.RabbitMqUtil;

import java.text.MessageFormat;

/**
 * @author : pankx
 * @description: 发布订阅模式-扇出交换机-随机队列
 * @date: 2023/9/4  16:49
 */
public class FanoutProducer {

    private static final String EXCHANGE_NAME = "fanout_logs";

    public static void main(String[] argv) throws Exception {
        try (Connection connection = RabbitMqUtil.newConnection();
             //创建链接通道
             Channel channel = connection.createChannel()) {
            //声明交换机(交换机名称，交换机类型)
             channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);
            int seq = 0;
            while (true) {
                String message = MessageFormat.format("第{0}发布订阅消息", seq);
                channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes("UTF-8"));
                //打印日志
                System.out.println(" [x] Sent '" + message + "'");
                seq++;
                //休眠1秒，每秒发送1条
                Thread.sleep(1000);
            }
        }
    }

}