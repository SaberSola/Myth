package com.github.myth.demo.springcloud.account.mq;

import com.github.myth.common.utils.LogUtil;
import com.github.myth.core.service.MythMqReceiveService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.List;

/**
 * <p>Description: .</p>
 *
 * @author xiaoyu(Myth)
 * @version 1.0
 * @date 2017/12/12 14:29
 * @since JDK 1.8
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.rocketmq", name = "namesrvAddr")
public class RocketmqConsumer {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RocketmqConsumer.class);
    private static final String TOPIC = "account";

    @Autowired
    private Environment env;

    @Autowired
    private MythMqReceiveService mythMqReceiveService;

    @Bean
    public DefaultMQPushConsumer pushConsumer() throws MQClientException {
        /**
         * 一个应用创建一个Consumer，由应用来维护此对象，可以设置为全局对象或者单例<br>
         * 注意：ConsumerGroupName需要由应用来保证唯一
         */
        DefaultMQPushConsumer consumer =
                new DefaultMQPushConsumer(env.getProperty("spring.rocketmq.consumerGroupName"));
        consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_FIRST_OFFSET);
        consumer.setNamesrvAddr(env.getProperty("spring.rocketmq.namesrvAddr"));
        consumer.setInstanceName(env.getProperty("spring.rocketmq.instanceName"));
        //设置批量消费，以提升消费吞吐量，默认是1
        consumer.setConsumeMessageBatchMaxSize(1);

        /**
         * 订阅指定topic下tags
         */
        consumer.subscribe(TOPIC, TOPIC);

        consumer.registerMessageListener((List<MessageExt> msgList, ConsumeConcurrentlyContext context) -> {

            MessageExt msg = msgList.get(0);
            try {
                // 默认msgList里只有一条消息，可以通过设置consumeMessageBatchMaxSize参数来批量接收消息
                final byte[] message = msg.getBody();
                LogUtil.debug(LOGGER, () -> "springcloud account-service rocketmq 接收到myth框架发出的信息====");
                final Boolean success = mythMqReceiveService.processMessage(message);
                if (success) {
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }

            } catch (Exception e) {
                e.printStackTrace();
                //重复消费
                return ConsumeConcurrentlyStatus.RECONSUME_LATER;
            }

            //如果没有return success，consumer会重复消费此信息，直到success。
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });

        /**
         * Consumer对象在使用之前必须要调用start初始化，初始化一次即可<br>
         */
        consumer.start();

        return consumer;
    }
}
