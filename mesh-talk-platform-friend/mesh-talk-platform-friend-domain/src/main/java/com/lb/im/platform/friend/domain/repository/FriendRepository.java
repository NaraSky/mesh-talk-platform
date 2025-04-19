package com.lb.im.platform.friend.domain.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lb.im.platform.common.model.entity.Friend;
import com.lb.im.platform.common.model.vo.FriendVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 好友数据访问层接口
 * 定义了与好友关系相关的数据库操作
 * 
 * 技术点：
 * 1. 基于MyBatis-Plus的BaseMapper，继承通用CRUD操作
 * 2. 使用MyBatis注解方式定义SQL语句，简化映射配置
 * 3. 实现了对im_friend表的完整数据访问操作
 * 4. 采用VO模式返回视图对象，优化数据传输
 * 5. 使用参数注解@Param标注SQL参数，增强可读性
 */
public interface FriendRepository extends BaseMapper<Friend> {

    /**
     * 获取用户的所有好友信息列表
     * 查询指定用户的所有好友基本信息，用于展示好友列表
     * 
     * @param userId 用户ID
     * @return 好友信息视图对象列表，包含好友ID、昵称和头像
     */
    @Select("select id as id, friend_nick_name as nickName, friend_head_image as headImage from im_friend where user_id = #{userId}")
    List<FriendVO> getFriendVOList(@Param("userId") Long userId);

    /**
     * 获取特定好友关系的信息
     * 查询用户与特定好友之间的关系信息
     * 
     * @param friendId 好友ID
     * @param userId 用户ID
     * @return 好友信息视图对象，包含好友ID、昵称和头像
     */
    @Select("select id as id, friend_nick_name as nickName, friend_head_image as headImage from im_friend where friend_id = #{friendId} and user_id = #{userId}")
    FriendVO getFriendVO(@Param("friendId") Long friendId, @Param("userId") Long userId);

    /**
     * 检查两个用户是否为好友关系
     * 通过查询是否存在对应的好友记录来判断好友关系
     * 
     * @param friendId 好友ID
     * @param userId 用户ID
     * @return 如果是好友返回1，否则返回null
     */
    @Select("select 1 from im_friend where friend_id = #{friendId} and user_id = #{userId} limit 1 ")
    Integer checkFriend(@Param("friendId") Long friendId, @Param("userId") Long userId);

    /**
     * 更新好友信息
     * 修改好友的昵称和头像信息
     * 
     * @param headImage 好友头像URL
     * @param nickName 好友昵称
     * @param friendId 好友ID
     * @param userId 用户ID
     * @return 影响的行数，成功更新返回1，否则返回0
     */
    @Update("update im_friend set friend_head_image = #{headImage}, friend_nick_name = #{nickName} where friend_id = #{friendId} and user_id = #{userId} ")
    int updateFriend(@Param("headImage") String headImage, @Param("nickName") String nickName, @Param("friendId") Long friendId, @Param("userId") Long userId);

    /**
     * 删除好友关系
     * 从数据库中移除指定的好友关系记录
     * 
     * @param friendId 好友ID
     * @param userId 用户ID
     * @return 影响的行数，成功删除返回1，否则返回0
     */
    @Delete("delete from im_friend where friend_id = #{friendId} and user_id = #{userId} ")
    int deleteFriend(@Param("friendId") Long friendId, @Param("userId") Long userId);

    /**
     * 获取用户的所有好友ID列表
     * 查询用户的所有好友ID，用于快速获取好友关系
     * 
     * @param userId 用户ID
     * @return 好友ID列表
     */
    @Select("select friend_id from im_friend where user_id = #{userId}")
    List<Long> getFriendIdList(@Param("userId") Long userId);

    /**
     * 获取用户的完整好友实体列表
     * 查询用户的所有好友完整信息，包含所有字段
     * 
     * @param userId 用户ID
     * @return 好友实体对象列表，包含完整的好友关系数据
     */
    @Select("select id as id, user_id as userId, friend_id as friendId, friend_nick_name as friendNickName, friend_head_image as friendHeadImage, created_time as createdTime " +
            "from im_friend where user_id = #{userId} ")
    List<Friend> getFriendByUserId(@Param("userId") Long userId);
}
