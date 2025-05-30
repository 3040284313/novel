package com.wtl.novel.Controller;


import com.wtl.novel.Service.DictionaryService;
import com.wtl.novel.entity.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dic")
public class DictionaryController {

    @Autowired
    private DictionaryService dictionaryService;

    @GetMapping("/getHome")
    public List<Dictionary> getPoint() {
        return dictionaryService.findByKeyFieldLikeAndIsDeletedFalse("/api/dic/getHome%");
    }

    @GetMapping("/getNovelDetail")
    public List<Dictionary> getNovelDetail() {
        return dictionaryService.findByKeyFieldLikeAndIsDeletedFalse("/api/dic/getNovelDetail%");
    }

}
