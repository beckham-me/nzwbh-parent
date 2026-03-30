package com.xs.nzwbh.pest.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xs.nzwbh.model.entity.CropDiseasesAndPests;
import com.xs.nzwbh.pest.mapper.CropPestMapper;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BloomFilterInitializer implements CommandLineRunner {


    @Autowired
    private RBloomFilter<String> pestBloomFilter;
    @Autowired
    private CropPestMapper cropPestMapper;

    @Override
    public void run(String... args) throws Exception {
        List<Long> ids = cropPestMapper.selectList(
                        new LambdaQueryWrapper<CropDiseasesAndPests>()
                                .eq(CropDiseasesAndPests::getIsDeleted, 0)
                                .select(CropDiseasesAndPests::getId))
                .stream()
                .map(CropDiseasesAndPests::getId)
                .collect(Collectors.toList());

        for (Long id : ids) {
            pestBloomFilter.add(String.valueOf(id));
        }
        System.out.println("布隆过滤器初始化完成，加载ID数量：" + ids.size());
    }
}
