package com.wtl.novel.Controller;

import com.wtl.novel.CDO.NovelSearchRequest;
import com.wtl.novel.CDO.TerminologyCTO;
import com.wtl.novel.Service.TerminologyService;
import com.wtl.novel.entity.Novel;
import com.wtl.novel.entity.Terminology;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/terminologies")
public class TerminologyController {

    @Autowired
    private TerminologyService terminologyService;


    // 根据平台和标签分页查询小说
    @PostMapping("/getTerminologyByPlatform")
    public Page<Terminology> getTerminologyByPlatform(
            @RequestBody TerminologyCTO request) {

        // 创建分页请求
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        // 调用服务层方法
        return terminologyService.getTerminologyByPlatform(
                request.getNovelId(),
                pageable);
    }

}