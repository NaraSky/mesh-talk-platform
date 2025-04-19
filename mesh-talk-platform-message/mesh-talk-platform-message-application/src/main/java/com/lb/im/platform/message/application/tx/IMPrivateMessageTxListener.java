package com.lb.im.platform.message.application.tx;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.platform.message.application.service.PrivateMessageService;
import com.lb.im.platform.message.domain.event.IMPrivateMessageTxEvent;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

/**
 * RocketMQ事务消息监听器 - 处理私聊消息的事务性发送
 * 
 * 该类实现了RocketMQ本地事务监听器接口，用于确保私聊消息在本地数据库保存成功后
 * 才提交消息到MQ，保证了消息发送的事务一致性。
 */
@Component
@RocketMQTransactionListener(/*txProducerGroup = SeckillConstants.TX_ORDER_PRODUCER_GROUP,*/ rocketMQTemplateBeanName = "rocketMQTemplate")
public class IMPrivateMessageTxListener implements RocketMQLocalTransactionListener {

    private final Logger logger = LoggerFactory.getLogger(IMPrivateMessageTxListener.class);

    @Autowired
    private PrivateMessageService privateMessageService;

    /**
     * 执行本地事务
     * 
     * 当RocketMQ接收到半消息后，会回调此方法执行本地事务
     * 在这里将私聊消息保存到数据库，并返回事务执行结果
     * 
     * @param message RocketMQ半消息
     * @param o 附加参数对象
     * @return 事务状态：提交/回滚/未知
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        try {
            // 从消息中解析出事务事件对象
            IMPrivateMessageTxEvent imPrivateMessageTxEvent = this.getTxMessage(message);
            // 调用服务保存消息到数据库
            boolean result = privateMessageService.saveIMPrivateMessageSaveEvent(imPrivateMessageTxEvent);
            if (result) {
                logger.info("executeLocalTransaction|消息微服务提交单聊本地事务成功|{}", imPrivateMessageTxEvent.getId());
                // 本地事务执行成功，提交消息
                return RocketMQLocalTransactionState.COMMIT;
            }
            logger.info("executeLocalTransaction|消息微服务提交单聊本地事务失败|{}", imPrivateMessageTxEvent.getId());
            // 本地事务执行失败，回滚消息
            return RocketMQLocalTransactionState.ROLLBACK;
        } catch (Exception e) {
            logger.info("executeLocalTransaction|消息微服务提交单聊本地事务异常|{}", e.getMessage(), e);
            // 发生异常，回滚消息
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 检查本地事务状态
     * 
     * 当RocketMQ不确定事务状态时，会回调此方法查询本地事务的执行结果
     * 用于解决本地事务执行成功但返回状态失败的情况
     * 
     * @param message RocketMQ半消息
     * @return 事务状态：提交/回滚/未知
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        // 从消息中解析出事务事件对象
        IMPrivateMessageTxEvent imPrivateMessageTxEvent = this.getTxMessage(message);
        logger.info("checkLocalTransaction|消息微服务查询单聊消息本地事务|{}", imPrivateMessageTxEvent.getId());
        // 检查消息是否已存在于数据库中
        Boolean submitTransaction = privateMessageService.checkExists(imPrivateMessageTxEvent.getId());
        // 如果消息存在，说明本地事务已成功执行，提交消息；否则返回未知状态，等待下次检查
        return BooleanUtil.isTrue(submitTransaction) ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.UNKNOWN;
    }

    /**
     * 从Message对象中提取私聊消息事务事件
     * 
     * @param msg RocketMQ消息对象
     * @return 私聊消息事务事件对象
     */
    private IMPrivateMessageTxEvent getTxMessage(Message msg) {
        // 将消息体转换为字符串
        String messageString = new String((byte[]) msg.getPayload());
        // 解析JSON对象
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        // 从JSON中提取事务消息
        String txStr = jsonObject.getString(IMConstants.MSG_KEY);
        // 将事务消息字符串转换为事件对象
        return JSONObject.parseObject(txStr, IMPrivateMessageTxEvent.class);
    }
}