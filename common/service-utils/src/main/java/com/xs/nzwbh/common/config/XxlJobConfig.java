package com.xs.nzwbh.common.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XxlJobConfig {

    private static final Logger logger = LoggerFactory.getLogger(XxlJobConfig.class);

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses; // XXL-JOB Admin 调度中心地址（如 http://localhost:8080/xxl-job-admin）

    @Value("${xxl.job.executor.appname}")
    private String appname; // 执行器注册到调度中心的应用名称（唯一标识）

    @Value("${xxl.job.executor.port}")
    private int port;

    @Value("${xxl.job.accessToken}")
    private String accessToken; // 与调度中心通信的鉴权令牌（安全校验）

    @Value("${xxl.job.executor.logpath}")
    private String logPath; // 任务执行日志本地存储路径

    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays; //执行日志保留天数，过期自动清理

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        logger.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        executor.setAdminAddresses(adminAddresses);
        executor.setAppname(appname);
        executor.setPort(port);
        executor.setAccessToken(accessToken);
        executor.setLogPath(logPath);
        executor.setLogRetentionDays(logRetentionDays);
        return executor;
    }
}
