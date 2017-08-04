package com.leo.alpha.service.sys.impl;

import com.leo.alpha.base.dao.CrudMapper;
import com.leo.alpha.base.service.impl.BaseCrudServiceImpl;
import com.leo.alpha.mapper.sys.ResourceMapper;
import com.leo.alpha.service.sys.ResourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 
 * @author leo
 * @date 2017-04-08 15:07:43
 * @version 1.0.0
 * @copyright www.leo.com
 */
@Service("resourceService")
public class ResourceServiceImpl extends BaseCrudServiceImpl implements ResourceService {

    @Resource
    private ResourceMapper resourceMapper;

    @Override
    public CrudMapper init() {
        return resourceMapper;
    }
}