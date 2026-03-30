package com.xs.nzwbh.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.xs.nzwbh.model.base.BaseEntity;
import lombok.Data;

@Data
@TableName("admin")
public class Admin extends BaseEntity {

    private String username;
    private String password;

}
