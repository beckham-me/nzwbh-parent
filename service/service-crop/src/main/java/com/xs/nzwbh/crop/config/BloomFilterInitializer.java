package com.xs.nzwbh.crop.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xs.nzwbh.crop.mapper.CropMapper;
import com.xs.nzwbh.model.entity.Crop;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BloomFilterInitializer implements CommandLineRunner {

    @Autowired
    private RBloomFilter<String> cropBloomFilter;
    @Autowired
    private CropMapper cropMapper;

    @Override
    public void run(String... args) {
        List<Long> ids = cropMapper.selectList(
                        new LambdaQueryWrapper<Crop>().eq(Crop::getIsDeleted, 0).select(Crop::getId))
                .stream()
                .map(Crop::getId)
                .collect(Collectors.toList());

        for (Long id : ids) {
            cropBloomFilter.add(String.valueOf(id));
        }
        System.out.println("作物布隆过滤器初始化完成，加载ID数量：" + ids.size());
    }
}
