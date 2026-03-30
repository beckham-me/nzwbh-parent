package com.xs.nzwbh.announcement.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.announcement.service.AnnouncementService;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.AnnouncementDto;
import com.xs.nzwbh.model.entity.Announcement;
import com.xs.nzwbh.model.vo.AnnouncementVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;


    @GetMapping("/latest")
    public Result<AnnouncementVo> getLatestAnnouncement() {
        AnnouncementVo announcementVo = announcementService.getLasterAnnouncement();
        return Result.ok(announcementVo);
    }


    //管理员端的方法
    @GetMapping
    public Result<Page<AnnouncementVo>> getAnnouncements(@RequestParam Integer page,
                                                       @RequestParam Integer size,
                                                       @RequestParam String keyword) {
        return announcementService.getAnnouncements(page, size, keyword);
    }

    @PostMapping("/add")
    public Result<Void> addAnnouncement(@RequestBody AnnouncementDto announcementdto) {
        return announcementService.addAnnouncement(announcementdto);
    }

    @PutMapping("/update")
    public Result<Void> updateAnnouncement(@RequestBody AnnouncementDto announcementdto) {
        return announcementService.updateAnnouncement(announcementdto);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        return announcementService.deleteAnnouncement(id);
    }
}
