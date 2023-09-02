package com.yourui.mq.util;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * @author : pankx
 * @description: 工具类
 * @date: 2023/8/31  19:24
 */
public class RabbitMqUtil {

    public static Connection newConnection() throws IOException, TimeoutException {
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
        Connection connection = factory.newConnection();
        return connection;

    }
}
