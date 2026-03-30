package com.xs.nzwbh.pest.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xs.nzwbh.model.entity.CropDiseasesAndPests;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CropPestMapper extends BaseMapper<CropDiseasesAndPests> {

    @Select("SELECT id, crop_id, name, type, image, description, cause, solution, " +
            "is_deleted, create_time, update_time " +
            "FROM crop_diseases_and_pests " +
            "WHERE crop_id = #{cropId} AND is_deleted = 0 " +
            "ORDER BY update_time DESC")
    List<CropDiseasesAndPests> selectByCropId(Long cropId);
}
