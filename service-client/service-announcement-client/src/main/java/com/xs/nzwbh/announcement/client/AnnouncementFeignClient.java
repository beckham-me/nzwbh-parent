package com.xs.nzwbh.announcement.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.AnnouncementDto;
import com.xs.nzwbh.model.vo.AnnouncementVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "service-announcement")
public interface AnnouncementFeignClient {

    @GetMapping("/announcement/latest")
    Result<AnnouncementVo> getLatestAnnouncement();

    @GetMapping("/announcement")
    public Result<Page<AnnouncementVo>> getAnnouncements(@RequestParam Integer page,
                                                         @RequestParam Integer size,
                                                         @RequestParam String keyword);
    @PostMapping("/announcement/add")
    public Result<Void> addAnnouncement(@RequestBody AnnouncementDto announcementdto);
    @PutMapping("/announcement/update")
    public Result<Void> updateAnnouncement(@RequestBody AnnouncementDto announcementdto);
    @DeleteMapping("/announcement/delete/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id);
}
