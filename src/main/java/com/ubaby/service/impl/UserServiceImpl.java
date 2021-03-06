package com.ubaby.service.impl;

import com.ubaby.common.Const;
import com.ubaby.common.ServerResponse;
import com.ubaby.common.TokenCache;
import com.ubaby.dao.UserMapper;
import com.ubaby.pojo.User;
import com.ubaby.service.UserService;
import com.ubaby.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * @author AlbertRui
 * @date 2018-05-04 20:13
 */
@SuppressWarnings("JavaDoc")
@Transactional
@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0)
            return ServerResponse.createByErrorMessage("用户名不存在");
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null)
            return ServerResponse.createByErrorMessage("密码错误");
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        ServerResponse<String> validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccess())
            return validResponse;
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess())
            return validResponse;

        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if (resultCount == 0)
            return ServerResponse.createByErrorMessage("注册失败");
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0)
                    return ServerResponse.createByErrorMessage("用户名已存在");
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0)
                    return ServerResponse.createByErrorMessage("email已存在");
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 密码提示接口
     *
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess())
            return ServerResponse.createByErrorMessage("用户不存在");//也就是调用checkValid方法校验成功
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question))
            return ServerResponse.createBySuccess(question);
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    /**
     * 校验密码提示问题
     *
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);

        //说明问题及问题的答案是这个用户的，并且是正确的
        if (resultCount > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }

        return ServerResponse.createByErrorMessage("答案错误！");
    }

    /**
     * 忘记密码后重置密码接口
     *
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return
     */
    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (StringUtils.isBlank(forgetToken))
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");

        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess())
            return ServerResponse.createByErrorMessage("用户不存在");

        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token))
            return ServerResponse.createByErrorMessage("token无效或者过期");

        if (StringUtils.equals(token, forgetToken)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);

            if (rowCount > 0)
                return ServerResponse.createBySuccessMessage("修改密码成功");
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 重置密码
     *
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(User user, String passwordOld, String passwordNew) {
        //防止横向越权，需要校验用户的旧密码
        int resultCount = userMapper.checkPassword(user.getId(), MD5Util.MD5EncodeUtf8(passwordOld));
        if (resultCount == 0)
            return ServerResponse.createByErrorMessage("旧密码错误");

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0)
            return ServerResponse.createBySuccessMessage("密码更新成功");

        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    /**
     * 更新用户信息
     * username是不能被更新的，并且email也要进行一个校验
     * 检查新的email是否已经存在，如果存在，不能是当前用户的
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse<User> updateUserInfo(User user) {
        int rowCount = userMapper.checkEmailByUserId(user.getId(), user.getEmail());
        if (rowCount > 0)
            return ServerResponse.createByErrorMessage("email已经存在，请更换email，并尝试更新");

        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0)
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);

        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    /**
     * 获取用户详细信息
     *
     * @return
     */
    @Override
    public ServerResponse<User> getUserDetails(Integer userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null)
            return ServerResponse.createByErrorMessage("找不到当前用户");

        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);

    }


    /*===============================backend===============================*/

    /**
     * 校验是否是管理员
     *
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> checkAdminRole(User user) {

        if (user != null && user.getRole() == Const.Role.ROLE_ADMIN)
            return ServerResponse.createBySuccess();

        return ServerResponse.createByError();

    }

}
