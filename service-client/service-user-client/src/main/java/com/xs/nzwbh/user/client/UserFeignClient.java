package com.xs.nzwbh.user.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.PageResult;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.FeedbackDTO;
import com.xs.nzwbh.model.entity.User;
import com.xs.nzwbh.model.mgrvo.UserVo;
import com.xs.nzwbh.model.vo.FeedbackVO;
import com.xs.nzwbh.model.vo.UserLoginVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-user")
public interface UserFeignClient  {

    @GetMapping("/user/getUserLoginInfo/{userId}")
    public Result<UserLoginVo> getUserLoginInfo(@PathVariable Long userId);

    @GetMapping("/user/login/{code}")
    public Result<Long> login(@PathVariable String code);

    @GetMapping("/user/getUserOpenId/{userId}")
    public Result<String> getUserOpenId(@PathVariable Long userId);

    @GetMapping("/user")
    public Result<Page<UserVo>> getUsers(@RequestParam Integer page,
                                         @RequestParam Integer size,
                                         @RequestParam String keyword);

    @GetMapping("/user/getUsername")
    public List<User> getUsersByIds(@RequestParam("userIds") List<Long> userIds);


    @PostMapping("/feedback/save")
    public Result<?> saveFeedback(@RequestParam("userId") Long userId,@RequestBody FeedbackDTO feedback);

    @GetMapping("/feedback/page")
    public Result<PageResult<List<FeedbackVO>>> page(@RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int limit);
    @DeleteMapping("/feedback/delete/{id}")
    public Result<?> delete(@PathVariable Long id);
}
