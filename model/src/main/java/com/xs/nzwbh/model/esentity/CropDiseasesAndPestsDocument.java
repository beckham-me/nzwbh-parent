package com.xs.nzwbh.model.esentity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

@Data
@Document(indexName = "crop_diseases_pests")
public class CropDiseasesAndPestsDocument {
    @Id
    private Long id;
    @Field(type = FieldType.Long)
    private Long cropId;
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String name;
    @Field(type = FieldType.Keyword)
    private String type;
    @Field(type = FieldType.Text)
    private String image;
    @Field(type = FieldType.Keyword)
    private String description;
    @Field(type = FieldType.Keyword)
    private String cause;
    @Field(type = FieldType.Keyword)
    private String solution;
    @Field(type = FieldType.Integer)
    private Integer isDeleted;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private Date createTime;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private Date updateTime;
}
