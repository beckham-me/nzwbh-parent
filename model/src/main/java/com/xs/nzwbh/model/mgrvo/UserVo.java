package com.xs.nzwbh.model.mgrvo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserVo {
    private Long id;

    private String nickname;

    private String gender;

    private String phone;

    private Integer status;
}
