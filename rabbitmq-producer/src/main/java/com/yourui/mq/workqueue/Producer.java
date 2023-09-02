package com.yourui.mq.workqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;
import com.yourui.mq.util.RabbitMqUtil;

import java.text.MessageFormat;

/**
 * @author : pankx
 * @description: 工作队列模式-生产者
 * @date: 2023/8/31  19:35
 */
public class Producer {
    private static final String TASK_QUEUE_NAME = "task_queue";

    public static void main(String[] argv) throws Exception {

        try (Connection connection = RabbitMqUtil.newConnection();
             Channel channel = connection.createChannel()) {
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            //等待接收消息的日志
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
            int seq =0;
            while(true){
                String message = MessageFormat.format("第{0}条工作队列消息",seq);
                channel.basicPublish("", TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
                seq++;
                //休眠1秒，每秒发送1条
                Thread.sleep(1000);
            }
        }
    }
}
