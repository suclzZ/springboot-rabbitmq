package com.sucl.amqp.configruation;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(AmqpProperties.class)
public class AmqpConfiguration {

    /**
     * 消息队列名称
     */
    public final static String QUEUE = "q.springboot-rabbitmq";
    /**
     * 空队列，不做任何处理，辅助延迟
     */
    public final static String QUEUE_NONE = "q.none.springboot-rabbitmq";
    /**
     * 延迟队列
     */
    public final static String QUEUE_DELAY = "q.delay.springboot-rabbitmq";
    /**
     * 消息交换机名
     */
    public final static String EXCHANGE = "x.springbot-rabbitmq";
    /**
     * 空队列消息路由键，和binding key相同，注意业务binding key用的是通配符匹配
     */
    public final static String ROUTING_KEY_NONE = "k.springbot-rabbitmq.none";
    /**
     * 消息路由键
     */
    public final static String ROUTING_KEY = "k.springbot-rabbitmq";
    /**
     * 队列与交换机绑定键(topic有用)
     */
    public final static String BINDING_KEY = "#.springbot-rabbitmq";
    /**
     * 死信交换机
     */
    public final static String EXCHANGE_DLX = "x.dlx.springbot-rabbitmq";
    /**
     * 延迟死信交换机
     */
    public final static String EXCHANGE_DLX_DELAY = "x.dlx.delay.springbot-rabbitmq";
    /**
     * 死信队列
     */
    public final static String QUEUE_DLX = "q.dlx.springbot-rabbitmq";
    /**
     * 死信routingKey
     */
    public final static String ROUTING_DLX_KEY = "k.dlx.springbot-rabbitmq";
    /**
     * 延迟死信routingKey
     */
    public final static String ROUTING_DLX_KEY_DELAY = "k.dlx.delay.springbot-rabbitmq";
    /**
     * 消息存活时间
     */
    public final static int MESSAGE_TTL = 10*1000;
    /**
     * 队列存活时间
     */
    public final static int QUEUE_TTL = 10*60*1000;

    /**
     * 业务的队列
     * @return
     */
    @Bean
    public Queue maintainQueue(){
        Map<String,Object> args = new HashMap<String,Object>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX);//死信交换机
        args.put("x-dead-letter-routing-key", ROUTING_DLX_KEY);//死信routingKey
        args.put("x-expires", QUEUE_TTL);//ms 队列过期时间
        args.put("x-message-ttl", MESSAGE_TTL);//消息过期时间
        return new Queue(QUEUE, true, false, false, args);
    }

    @Bean
    public Queue noneQueue(){
        Map<String,Object> args = new HashMap<String,Object>();
        args.put("x-dead-letter-exchange", EXCHANGE_DLX_DELAY);//死信交换机
        args.put("x-dead-letter-routing-key", ROUTING_DLX_KEY_DELAY);//死信routingKey
//        args.put("x-expires", QUEUE_TTL);//ms 队列过期时间
        args.put("x-message-ttl", 10000);//消息过期时间
        return new Queue(QUEUE_NONE, true, false, false, args);
    }

    @Bean
    public Queue delayQueue(){
        Map<String,Object> args = new HashMap<String,Object>();
        return new Queue(QUEUE_DELAY, true, false, false, args);
    }

    /**
     * 延迟业务路由
     * @return
     */
    @Bean
    public DirectExchange dealyExchange(){
        return new DirectExchange(EXCHANGE_DLX_DELAY,true,false,null);
    }

    /**
     * 将延迟业务队列绑定到延迟路由上
     * @return
     */
    @Bean
    public Binding delayBinding(){
        return BindingBuilder.bind(delayQueue()).to(dealyExchange()).with(ROUTING_DLX_KEY_DELAY);
    }

    /**
     * 业务路由
     * @return
     */
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE,true,false,null);
    }

    /**
     * 将业务队列绑定到业务路由上
     * @return
     */
    @Bean
    public Binding maintainBinding(){
        return BindingBuilder.bind(maintainQueue()).to(topicExchange()).with(BINDING_KEY);
    }

    @Bean
    public Binding noneBinding(){
        return BindingBuilder.bind(noneQueue()).to(topicExchange()).with(ROUTING_KEY_NONE);
    }

    /**
     * 死信队列
     * @return
     */
    @Bean
    public Queue dlxQueue(){
        return new Queue(QUEUE_DLX,false,false,false,null);
    }

    /**
     * 死信队列路由
     * @return
     */
    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(EXCHANGE_DLX,true,false,null);
    }

    /**
     * 将死信队列绑定到死信路由
     * @return
     */
    @Bean
    public Binding dlxBindging(){
        return BindingBuilder.bind(dlxQueue()).to(directExchange()).with(ROUTING_DLX_KEY);
    }

    /**
     * 重试，自动提交使用
     * @return
     */
    public MessageRecoverer messageRecoverer(RabbitTemplate rabbitTemplate){
        //交由路由处理
        new RepublishMessageRecoverer(rabbitTemplate,"x.error.x1","k.error.k1");
        //仅仅是打印结果，默认，针对nack但没有requeuing的队列进行重试，注意不是mq服务器的重试
        return new RejectAndDontRequeueRecoverer();
    }
}
