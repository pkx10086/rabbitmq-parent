package com.yourui.mq.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yourui.mq.util.RabbitMqUtil;

import java.time.Duration;
import java.util.UUID;

/**
 * 类功能描述：单独发布消息
 *
 * @author pankx$
 * @date 2023/9/24$
 */
public class IndividuallyPublishMessage {
    static final int MESSAGE_COUNT = 200;

    public static void main(String[] args) throws Exception {
        //单独发布消息
        publishMessagesIndividually();
    }
    /**
     * 单独发布消息
     * @throws Exception 例外
     */
    static void publishMessagesIndividually() throws Exception {
        //建立链接connect
        try (Connection connection = RabbitMqUtil.newConnection()) {
            //创建通道
            Channel ch = connection.createChannel();
            //生成随机队列
            String queue = UUID.randomUUID().toString();

            /**
             * 声明（创建）队列
             * queue      参数1：队列名称
             * durable    参数2：是否定义持久化队列,当mq重启之后,数据还存在
             * exclusive  参数3：是否独占本次连接
             *            ① 是否独占,只能有一个消费者监听这个队列
             *            ② 当connection关闭时,是否删除队列
             * autoDelete 参数4：是否在不使用的时候自动删除队列,当没有consumer时,自动删除
             * arguments  参数5：队列其它参数
             */
            ch.queueDeclare(queue, false, false, true, null);
            //开启确认模式
            ch.confirmSelect();
            long start = System.nanoTime();
            //循环发送50条数据
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                //推送的消息
                String body = String.valueOf(i);
                /*推送消息
                 *交换机命名，不填写使用默认的交换机
                 * routingKey -路由键-
                 * props:消息的其他属性-路由头等正文
                 * msg消息正文
                 */
                ch.basicPublish("", queue, null, body.getBytes());
                //如果超时过期，则抛出TimeoutException。如果任何消息被nack(丢失）, waitForConfirmsOrDie将抛出IOException。
                ch.waitForConfirmsOrDie(5_000);
            }
            long end = System.nanoTime();
            System.out.format("Published %,d messages individually in %,d ms%n", MESSAGE_COUNT, Duration.ofNanos(end - start).toMillis());
        }
    }
}
