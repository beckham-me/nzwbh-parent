package com.xs.nzwbh.announcement.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.AnnouncementDto;
import com.xs.nzwbh.model.entity.Announcement;
import com.xs.nzwbh.model.vo.AnnouncementVo;

public interface AnnouncementService {
    AnnouncementVo getLasterAnnouncement();




    Result<Void> addAnnouncement(AnnouncementDto announcementdto);

    Result<Void> updateAnnouncement(AnnouncementDto announcementdto);

    Result<Void> deleteAnnouncement(Long id);

    Result<Page<AnnouncementVo>> getAnnouncements(Integer page, Integer size, String keyword);
}
