package com.xs.nzwbh.crop.es;


import com.xs.nzwbh.model.esentity.CropDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CropRepository extends ElasticsearchRepository<CropDocument, Long> {
    List<CropDocument> findByNameContaining(String keyword);
}
