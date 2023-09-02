package com.yourui.mq.workqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;
import com.yourui.mq.util.RabbitMqUtil;

/**
 * @author : pankx
 * @description:工作队列模式消费者
 * @date: 2023/8/31  20:09
 */
public class ConsumerAck {
    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        //创建Connection
        final Connection connection = RabbitMqUtil.newConnection();
        //创建通道
        final Channel channel = connection.createChannel();
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
        channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
        //指该消费者在接收到队列里的消息但没有返回确认结果之前,它不会将新的消息分发给它。
        channel.basicQos(1);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            try {
                //模拟工作场景的代码
                doWork(message);
            } finally {
                System.out.println(" [x] Done");
                /**用于向RabbitMQ服务器发送确认消息，告诉服务器已成功接收并处理了一个或多个消息。
                 * 该方法的使用通常包括以下几个参数：
                 * deliveryTag：一个整数，表示消息的投递标识。每个消息都有一个唯一的投递标识，在消费者接收消息时会从消息的属性中获取。
                 * multiple：一个布尔值，表示是否批量确认。如果设置为true，将确认所有小于等于deliveryTag的未确认消息。如果设置为false，只确认当前deliveryTag指定的消息。
                 * */
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };

        /**参数：
         1. queue：队列名称
         2. autoAck：是否自动确认 ,类似咱们发短信,发送成功会收到一个确认消息(如果自动签收，会删除队列的该条消息)
         3. DeliverCallback：回调对象
         4、CancelCallback :中断正在执行的调度任务
         */

        //消费者类似一个监听程序,主要是用来监听消息
        channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
        });
    }

    private static void doWork(String message) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
