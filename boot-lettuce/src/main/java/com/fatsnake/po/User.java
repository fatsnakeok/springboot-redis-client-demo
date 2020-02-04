package com.fatsnake.po;

import lombok.Data;

import java.io.Serializable;

/**
 * @Auther: fatsnake
 * @Description":
 * @Date:2020-01-27 09:10
 * Copyright (c) 2020, zaodao All Rights Reserved.
 */
@Data
public class User implements Serializable {
    private String id;
    private String name;
    private Integer age;
    private Integer readNum;
}
