package com.xs.nzwbh.pest.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.CropDiseasesAndPestsDto;
import com.xs.nzwbh.model.mgrvo.DiseasesAndPestsVo;
import com.xs.nzwbh.model.vo.CropDiseasesAndPestsVo;
import com.xs.nzwbh.model.vo.SearchVo;
import com.xs.nzwbh.pest.service.CropPestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/diseases and pests")
public class CropPestController {

    @Autowired
    private CropPestService cropPestService;

    @GetMapping("/diseases/{cropId}")
    public Result<List<CropDiseasesAndPestsVo>> getDiseasesInfo(@PathVariable Long cropId) {
        List<CropDiseasesAndPestsVo> cropDiseasesAndPestsInfo = cropPestService.getCropDiseasesInfo(cropId);
        return Result.ok(cropDiseasesAndPestsInfo);
    }
    @GetMapping("/pests/{cropId}")
    public Result<List<CropDiseasesAndPestsVo>> getPestsInfo(@PathVariable Long cropId) {
        List<CropDiseasesAndPestsVo> cropDiseasesAndPestsInfo = cropPestService.getCropPestsInfo(cropId);
        return Result.ok(cropDiseasesAndPestsInfo);
    }
    @GetMapping("/{id}")
    public Result<CropDiseasesAndPestsVo> getCropDiseasesAndPestsInfo(@PathVariable Long id) {
        CropDiseasesAndPestsVo cropDiseasesAndPestsInfo = cropPestService.getCropDiseasesAndPestsInfo(id);
        return Result.ok(cropDiseasesAndPestsInfo);
    }
    @GetMapping("/common")
    public Result<List<CropDiseasesAndPestsVo>> getCommonCropDiseasesAndPestsInfo() {
        List<CropDiseasesAndPestsVo> common = cropPestService.getCommonCropDiseasesAndPestsInfo();
        return Result.ok(common);
    }


    @GetMapping("/most-similar")
    public SearchVo findMostSimilarName(@RequestParam("keyword") String keyword) {
        return cropPestService.findMostSimilarName(keyword);
    }
    @GetMapping("/findSimilar")
    public List<String> findSimilarDiseaseAndPestNames(@RequestParam("keyword") String keyword) {
        return cropPestService.findSimilarDiseaseAndPestNames(keyword);
    }

    @GetMapping
    public List<String> getPestNames(@RequestParam Long cropId) {
        return cropPestService.getrPestNames(cropId);
    }

    //管理员端的方法
    @GetMapping("/list")
    public Result<Page<DiseasesAndPestsVo>> getCropDiseasesAndPests(@RequestParam Integer page,
                                                                    @RequestParam Integer size,
                                                                    @RequestParam String keyword) {
        return cropPestService.getCropDiseasesAndPests(page, size, keyword);
    }

    @PostMapping("/add")
    public Result<Void> addCropDiseaseAndPest(@RequestBody CropDiseasesAndPestsDto cropDiseasesAndPestsDto) {
        return cropPestService.addCropDiseaseAndPest(cropDiseasesAndPestsDto);
    }

    @PutMapping("/update")
    public Result<Void> updateCropDiseaseAndPest(@RequestBody CropDiseasesAndPestsDto cropDiseasesAndPestsDto) {
        return cropPestService.updateCropDiseaseAndPest(cropDiseasesAndPestsDto);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCropDiseaseAndPest(@PathVariable Long id) {
        return cropPestService.deleteCropDiseaseAndPest(id);
    }

}
