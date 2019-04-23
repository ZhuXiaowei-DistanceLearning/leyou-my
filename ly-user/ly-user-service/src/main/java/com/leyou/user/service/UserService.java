package com.leyou.user.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.CodecUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public static final String KEY_PREFFIX = "user:verify:phone";

    public Boolean checkData(String data, Integer type) {
        // 判断数据类型
        User user = new User();
        switch (type) {
            case 1:
                user.setUsername(data);
                break;
            case 2:
                user.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnums.INVAILD_USER_DATA_TYPE);
        }
        return userMapper.selectCount(user) == 0;
    }

    public void sendCode(String phone) {
        // 生成key
        String key = KEY_PREFFIX + phone;
        // 生成验证码
        String code = NumberUtils.generateCode(6);
        Map<String, String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code", code);
        // 发送验证码
        amqpTemplate.convertAndSend("ly.sms.exchange", "sms.verify.code", msg);
        // 保存验证码
        redisTemplate.opsForValue().set(key, code, 5, TimeUnit.MINUTES);
    }

    public Boolean register(User user, String code) {
//        String key = KEY_PREFIX + user.getPhone();
//         从redis取出验证码
//        String codeCache = this.redisTemplate.opsForValue().get(key);
        String codeCache = "123456";
        // 检查验证码是否正确
        user.setId(null);
        user.setCreated(new Date());
        // 生成盐
        String salt = CodecUtils.generateSalt();
        user.setSalt(salt);
        // 对密码进行加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(), salt));
        user.setPhone("17342614429");
        // 写入数据库
        boolean boo = this.userMapper.insertSelective(user) == 1;

        // 如果注册成功，删除redis中的code
//        if (boo) {
//            try {
//                this.redisTemplate.delete(key);
//            } catch (Exception e) {
//                logger.error("删除缓存验证码失败，code：{}", code, e);
//            }
//        }
        return boo;
    }

    public User queryUser(String username, String password) {
        // 查询
        User record = new User();
        record.setUsername(username);
        User user = this.userMapper.selectOne(record);
        // 校验用户名
        if (user == null) {
            throw new LyException(ExceptionEnums.INVAILD_USERNAME_PASSWORD);
        }
        // 校验密码
        if (!user.getPassword().equals(CodecUtils.md5Hex(password, user.getSalt()))) {
            throw new LyException(ExceptionEnums.INVAILD_USERNAME_PASSWORD);
        }
        // 用户名密码都正确
        return user;
    }
}
