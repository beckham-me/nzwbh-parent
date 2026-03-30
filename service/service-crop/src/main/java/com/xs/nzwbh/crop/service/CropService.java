package com.xs.nzwbh.crop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.CropDto;
import com.xs.nzwbh.model.entity.Crop;
import com.xs.nzwbh.model.mgrvo.Crop1Vo;
import com.xs.nzwbh.model.vo.CropVo;
import com.xs.nzwbh.model.vo.SearchVo;

import java.util.List;

public interface CropService extends IService<Crop> {
    CropVo getById(Long id);

    List<CropVo> getAllCrops();

    SearchVo findMostSimilarName(String keyword);

    List<String> findSimilarCropNames(String keyword);

    //管理员端的方法
    Result<Page<Crop1Vo>> getCrops(Integer page, Integer size, String keyword);

    Result<Void> addCrops(CropDto cropdto);

    Result<Void> updateCrops(CropDto cropdto);

    Result<Void> deleteCrops(Long id);


    
}
