package com.sucl.amqp.server;

import com.sucl.amqp.configruation.AmqpConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 消息会优先转换成org.springframework.amqp.core.Message格式进行发送
 */
@Slf4j
@Component
public class MessageSender implements RabbitTemplate.ReturnCallback ,RabbitTemplate.ConfirmCallback{
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init(){
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
        rabbitTemplate.setMandatory(true);//队列不存在，会进行重试
    }

    public void send(String message){
        rabbitTemplate.convertAndSend(AmqpConfiguration.EXCHANGE,AmqpConfiguration.ROUTING_KEY,message);
    }

    public void delaySend(String message){
        rabbitTemplate.convertAndSend(AmqpConfiguration.EXCHANGE,AmqpConfiguration.ROUTING_KEY_NONE,message);
    }

    /**
     * 消息发送成功/失败监控
     * @param correlationData
     * @param ack
     * @param cause
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if(ack){
            log.info("消息发送成功");
        }else{
            //TODO 如果失败如何处理？
            log.warn("消息发送失败:{}",cause);
        }
    }

    /**
     * 当路由不到队列时返回给消息发送者
     * mandatory=true，否则直接丢弃消息
     * @param message
     * @param replyCode
     * @param replyText
     * @param exchange
     * @param routingKey
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        log.info("消息重发:{}",message.getMessageProperties().getMessageId());
        //TODO 消息发送到服务器失败，如何处理？重新发送？记录？
    }
}
