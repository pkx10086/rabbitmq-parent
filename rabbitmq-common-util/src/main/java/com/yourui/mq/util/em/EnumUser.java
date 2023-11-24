package com.yourui.mq.util.em;


import javax.sound.midi.Soundbank;

/**
 * 类功能描述：枚举类，表示用户的级别
 *
 * @author pankx
 * @date 2023/11/23$
 */
public enum EnumUser implements IBaseEnum<Integer>{

    ONE(1,"初级用户"),
    TWO(2,"中级用户"),
    THREE(3,"高级用户");

    /**
     * 枚举类的构造方法
     *
     * @param code   用户代码
     * @param message 用户消息
     */
    EnumUser(int code, String message) {
       initEnum(code, message);
    }

    /**
     * 主方法，用于测试枚举类的功能
     *
     * @param args 用户输入的参数
     */
    public static void main(String[] args) {
        System.out.println(""+EnumUser.ONE.getCode());
        System.out.println(EnumUser.ONE.getMessage());
        System.out.println(IBaseEnum.getCode(EnumUser.class, 1).getMessage());
        System.out.println(IBaseEnum.getCodes(EnumUser.class));
    }
}
