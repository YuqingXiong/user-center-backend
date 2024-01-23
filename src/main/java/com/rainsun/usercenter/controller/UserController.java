package com.rainsun.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rainsun.usercenter.constant.UserConstant;
import com.rainsun.usercenter.model.domain.User;
import com.rainsun.usercenter.model.domain.request.UserLoginRequest;
import com.rainsun.usercenter.model.domain.request.UserRegisterRequest;
import com.rainsun.usercenter.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.rainsun.usercenter.constant.UserConstant.ADMIN_ROLE;
import static com.rainsun.usercenter.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author rainsun
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest == null){
            return null;
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)){
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){
        if(userLoginRequest == null){
            return null;
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount, userPassword)){
            return null;
        }
        return userService.userLogin(userAccount, userPassword, request);
    }

    @GetMapping("/search")
    public List<User> searchUsers(String username, HttpServletRequest request){
        // 鉴权：仅管理员可查询
        if(!isAdmin(request)){
            return new ArrayList<>();
        }

        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if(StringUtils.isBlank(username)){
            wrapper.like("username", username);
        }
        List<User> userList = userService.list(wrapper);

        return userList.stream().map(user -> userService.getSaftetyUser(user)).collect(Collectors.toList());
    }

    @PostMapping("/delete")
    public boolean deleteUser(@RequestBody Long id, HttpServletRequest request){
        if(id < 0) return false;
        return isAdmin(request) && userService.removeById(id);
    }

    /**
     * 是否为管理员
     * @param request
     * @return
     */
    public boolean isAdmin(HttpServletRequest request){
        // 鉴权：仅管理员可操作
        User userObject = (User)request.getSession().getAttribute(USER_LOGIN_STATE);
        return userObject != null && userObject.getUserRole() == ADMIN_ROLE;
    }
}
