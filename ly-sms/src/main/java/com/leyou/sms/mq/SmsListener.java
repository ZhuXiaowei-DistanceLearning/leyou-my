package com.leyou.sms.mq;

import com.leyou.common.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private SmsProperties prop;


    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue", durable = "true"), exchange = @Exchange(name = "ly.item.exchange", type = ExchangeTypes.TOPIC), key = "sms.verify.code"
    ))
    public void listenInsertOrUpdate(Map<String, String> msg) {
        if (CollectionUtils.isEmpty(msg)) {
            return;
        }
        String phone = msg.remove("phone");
        if (StringUtils.isBlank(phone)) {
            return;
        }
        // 处理消息，对索引库进行新增或修改
        smsUtils.sendSms(phone, prop.getSignName(), prop.getVerifyCodeTemplate(), JsonUtils.serialize(msg));
    }
}
