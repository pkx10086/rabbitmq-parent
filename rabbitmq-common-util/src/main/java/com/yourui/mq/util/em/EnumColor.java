package com.yourui.mq.util.em;


/**
 * 类功能描述：枚举类，表示用户的级别
 *
 * @author pankx
 * @date 2023/11/23$
 */
public enum EnumColor implements IBaseEnum<String>{

    RED("red","红色"),
    BLUE("blue","蓝色"),
    ORANGE("orange","橘色");

    /**
     * 枚举类的构造方法
     *
     * @param code   用户代码
     * @param message 用户消息
     */
    EnumColor(String code, String message) {
       initEnum(code, message);
    }

    /**
     * 主方法，用于测试枚举类的功能
     *
     * @param args 用户输入的参数
     */
    public static void main(String[] args) {
        System.out.println(EnumColor.RED.getCode());
        System.out.println( EnumColor.RED.getMessage());
        System.out.println(IBaseEnum.getCode(EnumColor.class, "red").getCode());
        System.out.println(IBaseEnum.getCodes(EnumColor.class));
    }
}
