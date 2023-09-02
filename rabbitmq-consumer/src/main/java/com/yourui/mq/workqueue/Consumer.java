package com.yourui.mq.workqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.yourui.mq.util.RabbitMqUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author : pankx
 * @description:消费者
 * @date: 2023/8/29  20:06
 */
public class Consumer {
    /** 创建队列Queue,通过信道获取队列的数据*/
    private final static String QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {
        Connection connection = RabbitMqUtil.newConnection();
        //创建channel链接通道
        Channel channel = connection.createChannel();
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
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);
        //等待接收消息的日志
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
            //打印接收消息的日志
            System.out.println(" [x] Received '" + message + "'");
        };
        /**参数：
        1. queue：队列名称
        2. autoAck：是否自动确认 ,类似咱们发短信,发送成功会收到一个确认消息(如果自动签收，会删除队列的该条消息)
        3. DeliverCallback：回调对象
         4、CancelCallback :中断正在执行的调度任务
        */

        //消费者类似一个监听程序,主要是用来监听消息
        channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
    }
}
