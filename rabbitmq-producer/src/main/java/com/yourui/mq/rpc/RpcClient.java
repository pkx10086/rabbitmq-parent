package com.yourui.mq.rpc;

/**
 * @author : pankx
 * @description:
 * @date: 2023/9/13  15:58
 */

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.yourui.mq.util.RabbitMqUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RpcClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";

    public RpcClient() throws IOException, TimeoutException {
        //创建链接
        connection = RabbitMqUtil.newConnection();
        //创建通道
        channel = connection.createChannel();
    }

    public static void main(String[] argv) {
        try (RpcClient fibonacciRpc = new RpcClient()) {
            for (int i = 0; i < 32; i++) {
                String i_str = Integer.toString(i);
                System.out.println(" [x] Requesting fib(" + i_str + ")");
                String response = fibonacciRpc.call(i_str);
                System.out.println(" [.] Got '" + response + "'");
            }
        } catch (IOException | TimeoutException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public String call(String message) throws IOException, InterruptedException, ExecutionException {
        // 随机生成一个correlationId
        final String corrId = UUID.randomUUID().toString();
        // 服务端回调的队列名(随机生成)
        String replyQueueName = channel.queueDeclare().getQueue();
        // 设置发送消息的一些参数
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        /**
         * 参数1：交换机名称,如果没有指定则使用默认""
         * 参数2：路由key,简单模式可以传递队列名称
         * 参数3：消息的其它配置信息，暂时可以不配置
         * 参数4：消息主体，这里为 UTF-8 格式的字节数组，可以有效地杜绝中文乱码。
         */
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));
        final CompletableFuture<String> response = new CompletableFuture<>();
        /**
         * 启动一个消费者，并返回服务端生成的标识
         * queue 队列名称
         * autoAck：true接收到传递过来的消息后acknowledged（应答服务），false：接收到消息后不应答服务器
         * deliverCallback：当一个消息发送过来的回调接口
         * cancelCallback:当一个消费者取消订阅时回调接口，取消订阅消费者订阅队列时除了使用{@link Channel#basicCancel}之外的的所有方式都会调用该回调方法。
         * @return 服务端生成的消费标识
         * */
        String ctag = channel.basicConsume(replyQueueName, true, (consumerTag, delivery) -> {
            // 客户端从回调队列获取消息，匹配与发送消息correlationId相同的消息为应答结果
            if (delivery.getProperties().getCorrelationId().equals(corrId)) {
                response.complete(new String(delivery.getBody(), "UTF-8"));
            }
        }, consumerTag -> {
        });
        String result = null;
        try {
            //reponse.get 加上超是时间，为了防止server端挂掉后，在重启还是客户端还是一直卡住不动。
            //大家可以尝试把超时时间去掉，先启动客户端在启动服务端，观察结果
            result = response.get(1000, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            e.printStackTrace();
        }
        channel.basicCancel(ctag);
        return result;
    }

    @Override
    public void close() throws IOException {
        connection.close();
    }
}