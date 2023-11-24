package com.yourui.mq.confirm;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.Connection;
import com.yourui.mq.util.RabbitMqUtil;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BooleanSupplier;

public class PublisherConfirms {

    static final int MESSAGE_COUNT = 200;
    public static void main(String[] args) throws Exception {
        //单独发布消息
        publishMessagesIndividually();
        //批量发布消息
       // publishMessagesInBatch();
        //异步处理发布者确认
       // handlePublishConfirmsAsynchronously();
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

    /**
     * 批量发布消息
     *
     * @throws Exception 例外
     * @author pankx
     * @date 2023/09/23
     */
    static void publishMessagesInBatch() throws Exception {
        try (Connection connection = RabbitMqUtil.newConnection()) {
            Channel ch = connection.createChannel();

            String queue = UUID.randomUUID().toString();
            ch.queueDeclare(queue, false, false, true, null);

            ch.confirmSelect();

            int batchSize = 100;
            int outstandingMessageCount = 0;

            long start = System.nanoTime();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String body = String.valueOf(i);
                ch.basicPublish("", queue, null, body.getBytes());
                outstandingMessageCount++;

                if (outstandingMessageCount == batchSize) {
                    ch.waitForConfirmsOrDie(5_000);
                    outstandingMessageCount = 0;
                }
            }

            if (outstandingMessageCount > 0) {
                ch.waitForConfirmsOrDie(5_000);
            }
            long end = System.nanoTime();
            System.out.format("Published %,d messages in batch in %,d ms%n", MESSAGE_COUNT, Duration.ofNanos(end - start).toMillis());
        }
    }

    /**
     * 异步处理发布确认
     *
     * @throws Exception 例外
     */
    static void handlePublishConfirmsAsynchronously() throws Exception {
        try (Connection connection = RabbitMqUtil.newConnection()) {
            Channel ch = connection.createChannel();

            String queue = UUID.randomUUID().toString();
            ch.queueDeclare(queue, false, false, true, null);

            ch.confirmSelect();

            ConcurrentNavigableMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<>();

            ConfirmCallback cleanOutstandingConfirms = (sequenceNumber, multiple) -> {
                if (multiple) {
                    ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(
                            sequenceNumber, true
                    );
                    confirmed.clear();
                } else {
                    outstandingConfirms.remove(sequenceNumber);
                }
            };

            ch.addConfirmListener(cleanOutstandingConfirms, (sequenceNumber, multiple) -> {
                String body = outstandingConfirms.get(sequenceNumber);
                System.err.format(
                        "Message with body %s has been nack-ed. Sequence number: %d, multiple: %b%n",
                        body, sequenceNumber, multiple
                );
                cleanOutstandingConfirms.handle(sequenceNumber, multiple);
            });

            long start = System.nanoTime();
            for (int i = 0; i < MESSAGE_COUNT; i++) {
                String body = String.valueOf(i);
                outstandingConfirms.put(ch.getNextPublishSeqNo(), body);
                ch.basicPublish("", queue, null, body.getBytes());
            }

            if (!waitUntil(Duration.ofSeconds(60), () -> outstandingConfirms.isEmpty())) {
                throw new IllegalStateException("All messages could not be confirmed in 60 seconds");
            }

            long end = System.nanoTime();
            System.out.format("Published %,d messages and handled confirms asynchronously in %,d ms%n", MESSAGE_COUNT, Duration.ofNanos(end - start).toMillis());
        }
    }

    /**
     * 等到
     *
     * @param timeout   超时
     * @param condition 条件
     * @return boolean
     * @throws InterruptedException 中断异常
     */
    static boolean waitUntil(Duration timeout, BooleanSupplier condition) throws InterruptedException {
        int waited = 0;
        while (!condition.getAsBoolean() && waited < timeout.toMillis()) {
            Thread.sleep(100L);
            waited += 100;
        }
        return condition.getAsBoolean();
    }

}