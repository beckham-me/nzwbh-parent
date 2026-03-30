package com.xs.nzwbh.common.login;


import com.xs.nzwbh.common.util.AuthContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Component
@Aspect
public class LoginAspecy {

    @Autowired
    private RedisTemplate redisTemplate;

    @Around("execution(* com.xs.nzwbh.*.controller.*.*(..)) && @annotation(login)")
    public Object login(ProceedingJoinPoint proceedingJoinPoint, Login login) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = attributes.getRequest().getHeader("token");
        System.out.println("Received token: " + token); // 调试日志

        if (StringUtils.isEmpty(token)) {
            throw new RuntimeException("token不能为空");
        }

        String userId = (String) redisTemplate.opsForValue().get(token);
        log.info("Redis获取的userId: {}", userId);
        if (StringUtils.isEmpty(userId)) {
            System.out.println("Token无效或已过期，无法找到 userId: " + token);
            throw new RuntimeException("token无效或已过期");
        }

        try {
            AuthContextHolder.setUserId(Long.parseLong(userId));
            log.info("AuthContextHolder设置的userId: {}", AuthContextHolder.getUserId());
        } catch (NumberFormatException e) {
            System.out.println("无效的userId: " + userId);
            throw new RuntimeException("无效的userId");
        }
        AuthContextHolder.setToken(token);

        return proceedingJoinPoint.proceed();
    }

}
