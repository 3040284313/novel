package com.wtl.novel.Service;


import com.wtl.novel.entity.Dictionary;
import com.wtl.novel.repository.DictionaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryService {

    @Autowired
    private DictionaryRepository dictionaryRepository;


    public List<Dictionary> getAllDictionaries() {
        return dictionaryRepository.findAll();
    }

    public Dictionary getDictionaryByKey(String keyField) {
        return dictionaryRepository.getDictionaryByKeyField(keyField);
    }

    public Dictionary saveDictionary(Dictionary dictionary) {
        return dictionaryRepository.save(dictionary);
    }

    public void deleteDictionary(Long id) {
        dictionaryRepository.deleteById(id);
    }

    public List<Dictionary> findByKeyFieldLikeAndIsDeletedFalse(String keyFieldPattern) {
        return dictionaryRepository.findByKeyFieldLikeAndIsDeletedFalse(keyFieldPattern);
    }
}