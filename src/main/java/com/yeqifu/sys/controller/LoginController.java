package com.yeqifu.sys.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yeqifu.sys.common.ActiverUser;
import com.yeqifu.sys.common.Constast;
import com.yeqifu.sys.common.ResultObj;
import com.yeqifu.sys.common.WebUtils;
import com.yeqifu.sys.entity.Loginfo;
import com.yeqifu.sys.entity.User;
import com.yeqifu.sys.service.ILoginfoService;
import com.yeqifu.sys.service.IUserService;
import com.yeqifu.sys.vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.crypto.hash.Md5Hash;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("login")
public class LoginController {
    @Resource
    private JavaMailSender mailSender;

    @Autowired
    private IUserService userService;

    @Autowired
    private ILoginfoService loginfoService;

    @PostMapping("sendEmail")
    public Map<String, Object> sendEmail(String loginname, String email, HttpSession httpSession) {
        HashMap<String, Object> map = new HashMap<>();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>();
        userQueryWrapper.eq("loginname", loginname);
        User one = userService.getOne(userQueryWrapper);
        if (one == null) {
            map.put("success", false);
            map.put("errorInfo", "???????????????!");
            return map;
        } else if (!email.equals(one.getEmail())) {
            map.put("success", false);
            map.put("errorInfo", "????????????!");
            return map;
        } else {
            String mailCode = WebUtils.getSixRandom();
            //?????????
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("1844246071@qq.com");                        //?????????
            message.setTo(email);                                       //?????????
            message.setSubject("??????????????????????????????-??????????????????");         //??????
            message.setText("???????????????????????????" + mailCode);            //????????????
            try {
                mailSender.send(message);
                System.out.println(mailCode);
                //???????????????session
                httpSession.setAttribute("mailCode", mailCode);
                httpSession.setAttribute("loginname", loginname);
                map.put("success", true);
                return map;
            } catch (MailException e) {
                e.printStackTrace();
                map.put("success", false);
                map.put("errorInfo", "??????????????????????????????!");
                return map;
            }
        }
    }

    @PostMapping("checkYzm")
    public Map<String, Object> checkYzm(String yzm, HttpSession httpSession) {
        HashMap<String, Object> map = new HashMap<>();
        if (StringUtils.isBlank(yzm)) {
            map.put("success", false);
            map.put("errorInfo", "?????????????????????");
            return map;
        }
        String mailCode = (String) httpSession.getAttribute("mailCode");
        String loginname = (String) httpSession.getAttribute("loginname");
        if (!yzm.equals(mailCode)) {
            map.put("success", false);
            map.put("errorInfo", "???????????????");
            return map;
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<User>();
        userQueryWrapper.eq("loginname", loginname);
        User one = userService.getOne(userQueryWrapper);
        try {
            //?????????
            String salt = IdUtil.simpleUUID().toUpperCase();
            one.setSalt(salt);
            //??????????????????
            one.setPwd(new Md5Hash(Constast.USER_DEFAULT_PWD, salt, 2).toString());
            userService.updateById(one);
            map.put("success", true);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            map.put("success", false);
            map.put("errorInfo", "?????????????????????????????????????????????!");
            return map;
        }

    }

    @RequestMapping("login")
    public ResultObj login(UserVo userVo, String code, HttpSession session) {
        //userVo  ?????????????????????
        // code    ???????????????
        // session  ???????????????????????????????????????????????????????????????????????????session???

        //???????????????session???????????????
        System.out.println("login/login?????????");
        String sessionCode = (String) session.getAttribute("code");
        if (code != null && sessionCode.equals(code)) {
            Subject subject = SecurityUtils.getSubject();
            System.out.println(subject);
            // UsernamePasswordToken
            AuthenticationToken token = new UsernamePasswordToken(userVo.getLoginname(), userVo.getPwd());
            try {
                //???????????????????????????
                subject.login(token);
                //??????subject????????????????????????user
                ActiverUser activerUser = (ActiverUser) subject.getPrincipal();
                //???user?????????session???
                WebUtils.getSession().setAttribute("user", activerUser.getUser());
                //??????????????????
                Loginfo entity = new Loginfo();
                entity.setLoginname(activerUser.getUser().getName() + "-" + activerUser.getUser().getLoginname());
                entity.setLoginip(WebUtils.getRequest().getRemoteAddr());
                entity.setLogintime(new Date());
                loginfoService.save(entity);

                return ResultObj.LOGIN_SUCCESS;
            } catch (AuthenticationException e) {
                e.printStackTrace();
                return ResultObj.LOGIN_ERROR_PASS;
            }
        } else {
            return ResultObj.LOGIN_ERROR_CODE;
        }

    }

    /**
     * ?????????????????????
     *
     * @param response
     * @param session
     * @throws IOException
     */
    @RequestMapping("getCode")
    public void getCode(HttpServletResponse response, HttpSession session) throws IOException {
        //?????????????????????????????????
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(116, 36, 4, 5);
        session.setAttribute("code", lineCaptcha.getCode());
        try {
            ServletOutputStream outputStream = response.getOutputStream();
            lineCaptcha.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
