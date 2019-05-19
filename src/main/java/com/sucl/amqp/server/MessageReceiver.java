package com.sucl.amqp.server;

import com.rabbitmq.client.Channel;
import com.sucl.amqp.configruation.AmqpConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 通过confirm模式处理消息持久化
 * 在使用basicNack/basicReject时，如果设置reququ=false，消息进入死信队列
 *
 */
@Slf4j
@Component
public class MessageReceiver {

    /**
     * 处理正常业务
     * @param channel
     * @param message
     */
    @RabbitListener(queues = AmqpConfiguration.QUEUE)
    @RabbitHandler
    public void receive(Channel channel, @Payload Message message){
        String messsageText = new String(message.getBody());
        try {
            if(!validate(messsageText)){
                log.info("接收消息：{}",messsageText);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
            }else{
                log.info("拒绝消息：{}",messsageText);
                //ack返回false，并重新回到队列,可以批量拒绝 multiple=true
            //    channel.basicNack(message.getMessageProperties().getDeliveryTag(), false,true);
                //拒绝消息,只能拒绝一条
                channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean validate(String messsageText) {
        return messsageText!=null && messsageText.indexOf("fuck")>-1;
    }

    /**
     * 处理死信
     * @param channel
     * @param message
     */
    @RabbitListener(queues = AmqpConfiguration.QUEUE_DLX)
    @RabbitHandler
    public void dlxHandle(Channel channel, Message message){
        log.info("死信消息：{}", new String(message.getBody()));
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 处理延迟
     * @param channel
     * @param message
     */
    @RabbitListener(queues = AmqpConfiguration.QUEUE_DELAY)
    @RabbitHandler
    public void delayHandle(Channel channel, Message message){
        log.info("延迟消息：{}", new String(message.getBody()));
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(),false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
