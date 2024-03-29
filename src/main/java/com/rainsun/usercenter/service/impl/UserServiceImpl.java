package com.rainsun.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rainsun.usercenter.model.domain.User;
import com.rainsun.usercenter.service.UserService;
import com.rainsun.usercenter.mapper.UserMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.rainsun.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author rainsun
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2024-01-22 16:51:58
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    private UserMapper userMapper;

    private static final String SALT = "rainsun";


    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 数据校验
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            // todo 修改为自定义异常
            return -1;
        }
        if(userAccount.length() < 4 ){
            return -1;
        }
        if(userPassword.length() < 8 || checkPassword.length() < 8){
            return -1;
        }
        // 没有特殊字符(只允许字符数字下划线)
        String validPattern = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(!matcher.find()){
            return -1;
        }
        // 密码和校验码相同
        if(!userPassword.equals(checkPassword)){
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(wrapper);
        if(count > 0){
            return -1;
        }
        // 2.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if(!saveResult){
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1. 数据校验
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            return null;
        }
        if(userAccount.length() < 4 ){
            return null;
        }
        if(userPassword.length() < 8){
            return null;
        }
        // 没有特殊字符(只允许字符数字下划线)
        String validPattern = "^[a-zA-Z0-9_]+$";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(!matcher.find()){
            return null;
        }

        // 2.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("userAccount", userAccount);
        wrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(wrapper);
        if(user == null){
            log.info("user login failed, userAccount cannot match userPassword");
            return null;
        }

        // 3. 用户脱敏
        User safetyUser = getSaftetyUser(user);

        // 4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safetyUser);

        return safetyUser;
    }

    /**
     * 用户脱敏
     * @param originUser
     * @return
     */
    @Override
    public User getSaftetyUser(User originUser){
        if(originUser == null){
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getPhone());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        return safetyUser;
    }

    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

}




