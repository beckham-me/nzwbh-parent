package com.xs.nzwbh.pest.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.CropDiseasesAndPestsDto;
import com.xs.nzwbh.model.entity.CropDiseasesAndPests;
import com.xs.nzwbh.model.mgrvo.DiseasesAndPestsVo;
import com.xs.nzwbh.model.vo.CropDiseasesAndPestsVo;
import com.xs.nzwbh.model.vo.SearchVo;

import java.util.List;

public interface CropPestService extends IService<CropDiseasesAndPests> {

    List<CropDiseasesAndPestsVo> getCropDiseasesInfo(Long cropId);

    List<CropDiseasesAndPestsVo> getCropPestsInfo(Long cropId);

    CropDiseasesAndPestsVo getCropDiseasesAndPestsInfo(Long id);

    List<CropDiseasesAndPestsVo> getCommonCropDiseasesAndPestsInfo();


    SearchVo findMostSimilarName(String keyword);

    List<String> findSimilarDiseaseAndPestNames(String keyword);

    List<String> getrPestNames(Long cropId);

    Result<Page<DiseasesAndPestsVo>> getCropDiseasesAndPests(Integer page, Integer size, String keyword);

    Result<Void> addCropDiseaseAndPest(CropDiseasesAndPestsDto cropDiseasesAndPestsDto);

    Result<Void> updateCropDiseaseAndPest(CropDiseasesAndPestsDto cropDiseasesAndPestsDto);

    Result<Void> deleteCropDiseaseAndPest(Long id);
}
