package com.xs.nzwbh.detect.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xs.nzwbh.detect.mapper.PestDetectionMapper;
import com.xs.nzwbh.detect.service.PestDetectionService;
import com.xs.nzwbh.model.entity.PestDetection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 虫害检测服务实现类
 * 优化点：
 * 1. 使用 Redis 缓存最新检测结果，减少数据库压力
 * 2. 分页查询替代硬编码 LIMIT，避免数据量过大
 * 3. 添加时间范围校验，避免无效查询
 * 4. 封装查询条件，提高代码复用性
 * 5. 添加详细日志，便于问题排查
 * 6. 使用只读事务优化数据库连接
 */
@Slf4j
@Service
@Transactional(readOnly = true)
public class PestDetectionServiceImpl extends ServiceImpl<PestDetectionMapper, PestDetection>
        implements PestDetectionService {

    // 注意：需要提前在数据库建立复合索引 idx_deleted_time(is_deleted, create_time)

    /**
     * 获取最新的一条检测记录
     * 使用 Redis 缓存，缓存 key = "latestDetection"，过期时间在配置中指定（例如 60 秒）
     * 当新增检测记录时，应使用 @CacheEvict 清除该缓存
     */
    @Override
    @Cacheable(value = "latestDetection", unless = "#result == null")
    public Optional<PestDetection> getLatestDetection() {
        log.debug("从数据库查询最新检测记录");
        QueryWrapper<PestDetection> query = new QueryWrapper<>();
        query.orderByDesc("create_time").last("LIMIT 1");
        PestDetection detection = getBaseMapper().selectOne(query);
        return Optional.ofNullable(detection);
    }

    /**
     * 获取热力图所需的分页数据
     * 使用分页替代硬编码 LIMIT，支持前端按需加载
     *
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param page      当前页码（从1开始）
     * @param size      每页大小
     * @return 分页结果
     */
    @Override
    public Page<PestDetection> getAllForHeatmap(LocalDateTime startTime,
                                                LocalDateTime endTime,
                                                int page,
                                                int size) {
        // 1. 参数校验
        validateTimeRange(startTime, endTime);
        log.debug("查询热力图数据，start={}, end={}, page={}, size={}",
                startTime, endTime, page, size);

        // 2. 构建分页对象
        Page<PestDetection> pageObj = new Page<>(page, size);

        // 3. 构建查询条件
        LambdaQueryWrapper<PestDetection> wrapper = buildHeatmapQueryWrapper(startTime, endTime);

        // 4. 执行分页查询
        Page<PestDetection> result = getBaseMapper().selectPage(pageObj, wrapper);
        log.debug("查询结果：总记录数={}", result.getTotal());
        return result;
    }

    /**
     * 新增检测记录时，清除最新检测缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = "latestDetection", allEntries = true)
    public boolean save(PestDetection entity) {
        log.info("新增虫害检测记录，id={}", entity.getId());
        return super.save(entity);
    }

    /**
     * 更新检测记录时，清除最新检测缓存
     */
    @Override
    @Transactional
    @CacheEvict(value = "latestDetection", allEntries = true)
    public boolean updateById(PestDetection entity) {
        log.info("更新虫害检测记录，id={}", entity.getId());
        return super.updateById(entity);
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 校验时间范围
     */
    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new IllegalArgumentException("开始时间不能晚于结束时间");
        }
    }

    /**
     * 构建热力图查询条件（可复用于其他统计场景）
     *
     * @param startTime 开始时间（可为null）
     * @param endTime   结束时间（可为null）
     * @return LambdaQueryWrapper
     */
    private LambdaQueryWrapper<PestDetection> buildHeatmapQueryWrapper(LocalDateTime startTime,
                                                                       LocalDateTime endTime) {
        LambdaQueryWrapper<PestDetection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PestDetection::getIsDeleted, 0)
                .ge(startTime != null, PestDetection::getCreateTime, startTime)
                .le(endTime != null, PestDetection::getCreateTime, endTime);
        return wrapper;
    }
}
