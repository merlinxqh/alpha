package com.leo.alpha.controller;

import com.leo.alpha.base.resp.RespMessage;
import com.leo.alpha.entity.sys.Resource;
import com.leo.alpha.redis.ObjectRedisTemplate;
import com.leo.alpha.service.sys.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by leo on 2017/8/3.
 */
@Controller
public class HelloController {

    @Autowired
    private ObjectRedisTemplate redisTemplate;

    @Autowired
    private ResourceService resourceService;

    @GetMapping("/addResource")
    public @ResponseBody
    RespMessage<?> addResource(){
//        Resource re=new Resource();
//        re.setCode(String.valueOf(IdWorker.generatorId()));
//        re.setName("商品列表");
//        re.setLongCode(re.getCode());
//        re.setType(1);
//        re.setUrl("/goods/list");
//        resourceService.add(re);
        List<Resource> list=resourceService.findByBiz(null);
        return new RespMessage<>(list);
    }

    @GetMapping("/redis")
    public @ResponseBody String redis(@RequestParam String val){
        redisTemplate.setString("test:key",val);
        return "success";
    }

    @RequestMapping("/hello/{name}")
    public String index(@PathVariable String name, ModelMap model){
        model.put("name", name);
        /**
         * 默认模板文件夹:src/main/resources/templates/目录下添加一个模板文件hello.html
         * 默认静态资源文件夹:src/main/resources/static
         *
         */
        return "hello";
    }
}
