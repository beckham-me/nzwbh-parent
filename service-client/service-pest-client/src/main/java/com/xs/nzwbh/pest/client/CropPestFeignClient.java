package com.xs.nzwbh.pest.client;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xs.nzwbh.common.result.Result;
import com.xs.nzwbh.model.dto.CropDiseasesAndPestsDto;
import com.xs.nzwbh.model.entity.CropDiseasesAndPests;
import com.xs.nzwbh.model.mgrvo.DiseasesAndPestsVo;
import com.xs.nzwbh.model.vo.CropDiseasesAndPestsVo;
import com.xs.nzwbh.model.vo.SearchVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "service-pest")
public interface CropPestFeignClient {

    @GetMapping("/diseases and pests/diseases/{cropId}")
    Result<List<CropDiseasesAndPestsVo>> getDiseasesInfo(@PathVariable Long cropId);
    @GetMapping("/diseases and pests/pests/{cropId}")
    Result<List<CropDiseasesAndPestsVo>> getPestsInfo(@PathVariable Long cropId);
    @GetMapping("/diseases and pests/{id}")
    Result<CropDiseasesAndPestsVo> getCropDiseasesAndPestsInfo(@PathVariable Long id);
    @GetMapping("/diseases and pests/common")
    Result<List<CropDiseasesAndPestsVo>> getCommonCropDiseasesAndPestsInfo();
    @GetMapping("/diseases and pests/most-similar")
    public SearchVo findMostSimilarName(@RequestParam("keyword") String keyword);
    @GetMapping("/diseases and pests/findSimilar")
    List<String> findSimilarDiseaseAndPestNames(@RequestParam("keyword") String keyword);
    @GetMapping("/diseases and pests")
    public List<String> getPestNames(@RequestParam Long cropId);

    @GetMapping("/diseases and pests/list")
    public Result<Page<DiseasesAndPestsVo>> getCropDiseasesAndPests(@RequestParam Integer page,
                                                                    @RequestParam Integer size,
                                                                    @RequestParam String keyword);
    @PostMapping("/diseases and pests/add")
    public Result<Void> addCropDiseaseAndPest(@RequestBody CropDiseasesAndPestsDto cropDiseasesAndPestsDto);
    @PutMapping("/diseases and pests/update")
    public Result<Void> updateCropDiseaseAndPest(@RequestBody CropDiseasesAndPestsDto cropDiseasesAndPestsDto);
    @DeleteMapping("/diseases and pests/delete/{id}")
    public Result<Void> deleteCropDiseaseAndPest(@PathVariable Long id);
}
