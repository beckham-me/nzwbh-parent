package com.xs.nzwbh.crop.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.crop.service.CropService;
import com.xs.nzwbh.model.dto.AnnouncementDto;
import com.xs.nzwbh.model.dto.CropDto;
import com.xs.nzwbh.model.entity.Crop;
import com.xs.nzwbh.model.mgrvo.Crop1Vo;
import com.xs.nzwbh.model.vo.AnnouncementVo;
import com.xs.nzwbh.model.vo.CropVo;
import com.xs.nzwbh.model.vo.SearchVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crop")
public class CropController {

    @Autowired
    private CropService cropService;

    @GetMapping
    public Result<List<CropVo>> getAllPlants() {
        return Result.ok(cropService.getAllCrops());
    }

    @GetMapping("/{id}")
    public Result<CropVo> getCropById(@PathVariable Long id) {
        return Result.ok(cropService.getById(id));
    }
    @GetMapping("/most-similar")
    public SearchVo findMostSimilarName(@RequestParam String keyword) {
        return cropService.findMostSimilarName(keyword);
    }

    @GetMapping("/findSimilar")
    public List<String> findSimilarCropNames(@RequestParam("keyword") String keyword) {
        return cropService.findSimilarCropNames(keyword);
    }

    //管理员端的方法
    @GetMapping("/list")
    public Result<Page<Crop1Vo>> getCrops(@RequestParam Integer page,
                                          @RequestParam Integer size,
                                          @RequestParam String keyword) {
        return cropService.getCrops(page, size, keyword);
    }

    @PostMapping("/add")
    public Result<Void> addCrop(@RequestBody CropDto cropdto) {
        return cropService.addCrops( cropdto);
    }

    @PutMapping("/update")
    public Result<Void> updateCrop(@RequestBody CropDto  cropdto) {
        return cropService.updateCrops( cropdto);
    }

    @DeleteMapping("/delete/{id}")
    public Result<Void> deleteCrop(@PathVariable Long id) {
        return cropService.deleteCrops(id);
    }
}
