package com.fatsnake.Service;

import com.fatsnake.po.User;

import java.lang.reflect.InvocationTargetException;

/**
 * @Auther: fatsnake
 * @Description":
 * @Date:2020-01-24 19:14
 * Copyright (c) 2020, zaodao All Rights Reserved.
 */
public interface IUserService {

    String getString(String key);

    void expireStr(String key, String value);

    User selectById(String id);

    Long incrementReadCount(String id);

    User getHashUserById (String id);

}
