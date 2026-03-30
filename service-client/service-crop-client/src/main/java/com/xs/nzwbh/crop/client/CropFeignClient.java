package com.xs.nzwbh.crop.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.CropDto;
import com.xs.nzwbh.model.mgrvo.Crop1Vo;
import com.xs.nzwbh.model.vo.CropVo;
import com.xs.nzwbh.model.vo.SearchVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-crop")
public interface CropFeignClient {
    @GetMapping("/crop/{id}")
    public Result<CropVo> getCropById(@PathVariable Long id);
    @GetMapping("/crop")
    public Result<List<CropVo>> getAllPlants();
    @GetMapping("/crop/similar")
    List<String> findSimilarNames(@RequestParam("keyword") String keyword);
    @GetMapping("/crop/most-similar")
    SearchVo findMostSimilarName(@RequestParam("keyword") String keyword);
    @GetMapping("/crop/findSimilar")
    public List<String> findSimilarCropNames(@RequestParam("keyword") String keyword);

    @GetMapping("/crop/list")
    public Result<Page<Crop1Vo>> getCrops(@RequestParam Integer page,
                                          @RequestParam Integer size,
                                          @RequestParam String keyword);
    @PostMapping("/crop/add")
    public Result<Void> addCrop(@RequestBody CropDto cropdto);
    @PutMapping("/crop/update")
    public Result<Void> updateCrop(@RequestBody CropDto  cropdto);
    @DeleteMapping("/crop/delete/{id}")
    public Result<Void> deleteCrop(@PathVariable Long id);

}
