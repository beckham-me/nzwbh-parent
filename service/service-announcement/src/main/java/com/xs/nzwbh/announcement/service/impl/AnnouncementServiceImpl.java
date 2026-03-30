package com.xs.nzwbh.announcement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.announcement.mapper.AnnouncementMapper;
import com.xs.nzwbh.announcement.service.AnnouncementService;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.AnnouncementDto;
import com.xs.nzwbh.model.entity.Announcement;
import com.xs.nzwbh.model.vo.AnnouncementVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.stream.Collectors;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {


    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public AnnouncementVo getLasterAnnouncement() {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Announcement::getUpdateTime).last("limit 1");
        Announcement announcement = announcementMapper.selectOne(queryWrapper);
        AnnouncementVo announcementVo = new AnnouncementVo();
        BeanUtils.copyProperties(announcement, announcementVo);
        return announcementVo ;
    }
    //以下是管理员端的方法
    @Override
    public Result<Page<AnnouncementVo>> getAnnouncements(Integer page, Integer size, String keyword) {
        // 查询数据库
        Page<Announcement> announcementPage = new Page<>(page, size);
        QueryWrapper<Announcement> queryWrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.like("title", keyword);
        }
        Page<Announcement> announcementPageResult = announcementMapper.selectPage(announcementPage, queryWrapper);

        // 将 Announcement 转换为 AnnouncementVo
        Page<AnnouncementVo> announcementVoPage = new Page<>();
        announcementVoPage.setCurrent(announcementPageResult.getCurrent());
        announcementVoPage.setSize(announcementPageResult.getSize());
        announcementVoPage.setTotal(announcementPageResult.getTotal());
        announcementVoPage.setPages(announcementPageResult.getPages());
        announcementVoPage.setRecords(announcementPageResult.getRecords().stream()
                .map(this::convertToVo)
                .collect(Collectors.toList()));
        return Result.ok(announcementVoPage);
    }

    /**
     * 将 Announcement 转换为 AnnouncementVo
     */
    private AnnouncementVo convertToVo(Announcement announcement) {
        AnnouncementVo vo = new AnnouncementVo();
        BeanUtils.copyProperties(announcement, vo);
        return vo;
    }

    @Override
    public Result<Void> addAnnouncement(AnnouncementDto announcementdto) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(announcementdto, announcement);
        announcement.setCreateTime(new Date());
        announcement.setUpdateTime(new Date());
        announcement.setIsDeleted(0);
        boolean success = announcementMapper.insert(announcement) > 0;
        return success ? Result.ok() : Result.fail();
    }

    @Override
    public Result<Void> updateAnnouncement(AnnouncementDto announcementdto) {
        // 确保 announcementdto 中的 id 是正确的
        if (announcementdto.getId() == null) {
            log.error("公告 ID 不能为空");
            return Result.fail();
        }
        // 查询数据库中已存在的公告
        Announcement announcement = announcementMapper.selectById(announcementdto.getId());
        if (announcement == null) {
            log.error("公告不存在");
            return Result.fail();
        }

        // 只更新需要更新的字段
        announcement.setTitle(announcementdto.getTitle());
        announcement.setContent(announcementdto.getContent());
        // 其他需要更新的字段...

        // 设置更新时间
        announcement.setUpdateTime(new Date());

        // 执行更新
        boolean success = announcementMapper.updateById(announcement) > 0;
        return success ? Result.ok() : Result.fail();
    }

    @Override
    public Result<Void> deleteAnnouncement(Long id) {
        boolean success = announcementMapper.deleteById(id) > 0;
        return success ? Result.ok() : Result.fail();
    }
}



















