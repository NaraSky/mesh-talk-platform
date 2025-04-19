package com.lb.im.platform.message.application.tx;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson.JSONObject;
import com.lb.im.common.domain.constans.IMConstants;
import com.lb.im.platform.common.model.constants.IMPlatformConstants;
import com.lb.im.platform.message.application.service.GroupMessageService;
import com.lb.im.platform.message.application.service.PrivateMessageService;
import com.lb.im.platform.message.domain.event.IMGroupMessageTxEvent;
import com.lb.im.platform.message.domain.event.IMMessageTxEvent;
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
 * 即时通讯消息事务监听器
 * 实现RocketMQ事务消息的本地事务处理和事务状态查询
 * 处理单聊和群聊两种类型的消息事务
 */
@Component
@RocketMQTransactionListener(rocketMQTemplateBeanName = "rocketMQTemplate")
public class IMMessageTxListener implements RocketMQLocalTransactionListener {
    // 日志记录器
    private final Logger logger = LoggerFactory.getLogger(IMMessageTxListener.class);

    // 单聊消息服务，处理单聊消息的业务逻辑
    @Autowired
    private PrivateMessageService privateMessageService;
    
    // 群聊消息服务，处理群聊消息的业务逻辑
    @Autowired
    private GroupMessageService groupMessageService;

    /**
     * 执行本地事务
     * 根据消息类型调用相应的事务处理方法
     *
     * @param message RocketMQ消息对象
     * @param o 附加参数对象
     * @return 事务状态：COMMIT(提交)、ROLLBACK(回滚)、UNKNOWN(未知)
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object o) {
        try {
            // 从消息中提取事务事件
            IMMessageTxEvent imMessageTxEvent = this.getTxMessage(message);
            
            // 根据消息类型执行相应的本地事务
            switch (imMessageTxEvent.getMessageType()) {
                // 单聊消息事务处理
                case IMPlatformConstants.TYPE_MESSAGE_PRIVATE:
                    return executePrivateMessageLocalTransaction(message);
                // 群聊消息事务处理
                case IMPlatformConstants.TYPE_MESSAGE_GROUP:
                    return executeGroupMessageLocalTransaction(message);
                // 默认按单聊消息处理
                default:
                    return executePrivateMessageLocalTransaction(message);
            }
        } catch (Exception e) {
            // 记录异常日志并回滚事务
            logger.info("executeLocalTransaction|消息微服务提交本地事务异常|{}", e.getMessage(), e);
            return RocketMQLocalTransactionState.ROLLBACK;
        }
    }

    /**
     * 执行群聊消息本地事务
     * 保存群聊消息到数据库
     *
     * @param message RocketMQ消息对象
     * @return 事务状态
     */
    private RocketMQLocalTransactionState executeGroupMessageLocalTransaction(Message message) {
        // 从消息中提取群聊消息事务事件
        IMGroupMessageTxEvent imGroupMessageTxEvent = this.getTxGroupMessage(message);
        
        // 调用服务保存消息
        boolean result = groupMessageService.saveIMGroupMessageTxEvent(imGroupMessageTxEvent);
        
        // 根据保存结果决定事务状态
        if (result) {
            logger.info("executeGroupMessageLocalTransaction|消息微服务提交群聊本地事务成功|{}", imGroupMessageTxEvent.getId());
            return RocketMQLocalTransactionState.COMMIT;
        }
        
        logger.info("executeGroupMessageLocalTransaction|消息微服务提交群聊本地事务失败|{}", imGroupMessageTxEvent.getId());
        return RocketMQLocalTransactionState.ROLLBACK;
    }

    /**
     * 执行单聊消息本地事务
     * 保存单聊消息到数据库
     *
     * @param message RocketMQ消息对象
     * @return 事务状态
     */
    private RocketMQLocalTransactionState executePrivateMessageLocalTransaction(Message message) {
        // 从消息中提取单聊消息事务事件
        IMPrivateMessageTxEvent imPrivateMessageTxEvent = this.getTxPrivateMessage(message);
        
        // 调用服务保存消息
        boolean result = privateMessageService.saveIMPrivateMessageSaveEvent(imPrivateMessageTxEvent);
        
        // 根据保存结果决定事务状态
        if (result) {
            logger.info("executePrivateMessageLocalTransaction|消息微服务提交单聊本地事务成功|{}", imPrivateMessageTxEvent.getId());
            return RocketMQLocalTransactionState.COMMIT;
        }
        
        logger.info("executePrivateMessageLocalTransaction|消息微服务提交单聊本地事务失败|{}", imPrivateMessageTxEvent.getId());
        return RocketMQLocalTransactionState.ROLLBACK;
    }

    /**
     * 检查本地事务状态
     * 用于RocketMQ回查事务状态，确保消息的可靠投递
     *
     * @param message RocketMQ消息对象
     * @return 事务状态
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {
        // 从消息中提取事务事件
        IMMessageTxEvent imMessageTxEvent = this.getTxMessage(message);
        logger.info("checkLocalTransaction|消息微服务查询本地事务|{}", imMessageTxEvent.getId());
        
        // 默认事务未提交
        Boolean submitTransaction = Boolean.FALSE;
        
        // 根据消息类型检查对应的消息是否存在
        switch (imMessageTxEvent.getMessageType()) {
            // 检查单聊消息
            case IMPlatformConstants.TYPE_MESSAGE_PRIVATE:
                submitTransaction = privateMessageService.checkExists(imMessageTxEvent.getId());
                break;
            // 检查群聊消息
            case IMPlatformConstants.TYPE_MESSAGE_GROUP:
                submitTransaction = groupMessageService.checkExists(imMessageTxEvent.getId());
                break;
            // 默认检查单聊消息
            default:
                submitTransaction = privateMessageService.checkExists(imMessageTxEvent.getId());
        }
        
        // 根据检查结果返回事务状态
        return BooleanUtil.isTrue(submitTransaction) ? RocketMQLocalTransactionState.COMMIT : RocketMQLocalTransactionState.UNKNOWN;
    }

    /**
     * 从消息中提取通用消息事务事件
     *
     * @param msg RocketMQ消息对象
     * @return 消息事务事件
     */
    private IMMessageTxEvent getTxMessage(Message msg) {
        String messageString = new String((byte[]) msg.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String txStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(txStr, IMMessageTxEvent.class);
    }

    /**
     * 从消息中提取单聊消息事务事件
     *
     * @param msg RocketMQ消息对象
     * @return 单聊消息事务事件
     */
    private IMPrivateMessageTxEvent getTxPrivateMessage(Message msg) {
        String messageString = new String((byte[]) msg.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String txStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(txStr, IMPrivateMessageTxEvent.class);
    }
    
    /**
     * 从消息中提取群聊消息事务事件
     *
     * @param msg RocketMQ消息对象
     * @return 群聊消息事务事件
     */
    private IMGroupMessageTxEvent getTxGroupMessage(Message msg) {
        String messageString = new String((byte[]) msg.getPayload());
        JSONObject jsonObject = JSONObject.parseObject(messageString);
        String txStr = jsonObject.getString(IMConstants.MSG_KEY);
        return JSONObject.parseObject(txStr, IMGroupMessageTxEvent.class);
    }
}
