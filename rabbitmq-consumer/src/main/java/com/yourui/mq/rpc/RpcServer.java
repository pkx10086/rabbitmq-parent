package com.yourui.mq.rpc;

/**
 * @author : pankx
 * @description:
 * @date: 2023/9/13  16:01
 */

import com.rabbitmq.client.*;
import com.yourui.mq.util.RabbitMqUtil;

public class RpcServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    private static int fib(int n) {
        if (n == 0) {
            return 0;
        }
        if (n == 1) {
            return 1;
        }
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] argv) throws Exception {

        //创建链接
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
        channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
        channel.queuePurge(RPC_QUEUE_NAME);

        channel.basicQos(1);
        //等待客户端请求
        System.out.println(" [x] Awaiting RPC requests");

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                    .Builder()
                    .correlationId(delivery.getProperties().getCorrelationId())
                    .build();

            String response = "";
            try {
                String message = new String(delivery.getBody(), "UTF-8");
                int n = Integer.parseInt(message);
                //斐波那契数列函数，只能接收正整数
                System.out.println(" [.] fib(" + message + ")");
                response += fib(n);
            } catch (RuntimeException e) {
                System.out.println(" [.] " + e);
            } finally {

                channel.basicPublish("", delivery.getProperties().getReplyTo(), replyProps, response.getBytes("UTF-8"));
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        // 接受到客户端的请求(消息)
        /**
         * 参数1：交换机名称,如果没有指定则使用默认""
         * 参数2：路由key,简单模式可以传递队列名称
         * 参数3：消息的其它配置信息，暂时可以不配置
         * 参数4：消息主体，这里为 UTF-8 格式的字节数组，可以有效地杜绝中文乱码。
         */
        channel.basicConsume(RPC_QUEUE_NAME, false, deliverCallback, (consumerTag -> {
        }));
    }
}