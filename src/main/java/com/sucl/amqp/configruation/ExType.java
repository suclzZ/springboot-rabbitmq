package com.sucl.amqp.configruation;

/**
 * @author sucl
 * @date 2019/5/14
 */
public enum ExType {
    /**
     * 匹配与所有ex绑定的队列
     */
    Fanout,
    /**
     * 匹配routingKey与bindingKey相同值
     */
    Direct,
    /**
     * 匹配bindingKey满足routingKey通配的值
     */
    Topic,
    /**
     *自定义匹配
     */
    Header;

    public String value(){
        return name().toLowerCase();
    }
}
