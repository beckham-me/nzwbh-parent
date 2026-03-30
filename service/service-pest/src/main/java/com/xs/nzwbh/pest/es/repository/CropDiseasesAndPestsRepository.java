package com.xs.nzwbh.pest.es.repository;

import com.xs.nzwbh.model.esentity.CropDiseasesAndPestsDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropDiseasesAndPestsRepository
        extends ElasticsearchRepository<CropDiseasesAndPestsDocument, Long> {
    List<CropDiseasesAndPestsDocument> findByNameContaining(String keyword);
    Page<CropDiseasesAndPestsDocument> findByNameContaining(String keyword, Pageable pageable);
    List<CropDiseasesAndPestsDocument> findByCropIdAndType(Long cropId, String type);
}