package com.yourui.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author : pankx
 * @description:
 * @date: 2023/8/29  19:55
 */
public class Producer {

    private final static String QUEUE_NAME = "hello";

    public static void main(String[] argv) throws Exception {
        //创建工厂链接
        ConnectionFactory factory = new ConnectionFactory();
        //主机地址
        factory.setHost("123.56.236.43");
        //端口地址，不写默认为 15672
        factory.setPort(5672);
        //虚拟主机名称;默认为
        factory.setVirtualHost("/");
        //连接用户名；默认为guest
        factory.setUsername("admin");
        //连接密码；默认为guest
        factory.setPassword("admin");
        //创建链接
        try (Connection connection = factory.newConnection();
             //创建channel链接通道
             Channel channel = connection.createChannel()) {

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

            channel.queueDeclare(QUEUE_NAME, false, false, false, null);
            //发送消息内容
            String message = "Hello World!";
            /**
             * 参数1：交换机名称,如果没有指定则使用默认""
             * 参数2：路由key,简单模式可以传递队列名称
             * 参数3：消息的其它配置信息，暂时可以不配置
             * 参数4：消息主体，这里为 UTF-8 格式的字节数组，可以有效地杜绝中文乱码。
             */
            while(true){
                channel.basicPublish("", QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
                //打印发送的消息
                System.out.println(" [x] Sent '" + message + "'");
                Thread.sleep(1000);
            }


        }
    }
}
