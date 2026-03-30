package com.xs.nzwbh.model.mgrvo;

import lombok.Data;

@Data
public class SearchHistory1Vo {


    private Long id;

    private String username;

    private String keyword;

    private String resultType;

    // 是否为热门搜索（0：否；1：是）
    private Integer isHot;

    private Long searchCount;

    private String source;

    private Integer isDeleted;
}
