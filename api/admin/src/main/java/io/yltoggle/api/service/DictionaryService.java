package io.yltoggle.api.service;

import io.yltoggle.api.auth.TokenHelper;
import io.yltoggle.api.dao.exception.ResourceNotFoundException;
import io.yltoggle.api.dto.DictionaryResponse;
import io.yltoggle.api.dao.entity.Dictionary;
import io.yltoggle.api.base.enums.ResourceType;
import io.yltoggle.api.mapper.DictionaryMapper;
import io.yltoggle.api.dao.repository.DictionaryRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import java.util.Optional;

@AllArgsConstructor
@Service
@Slf4j
public class DictionaryService {

    private DictionaryRepository dictionaryRepository;

    public DictionaryResponse create(String key, String value) {
        String accountHash = "";
        if (StringUtils.isNotBlank(TokenHelper.getAccount())) {
            accountHash = DigestUtils.md5Hex(TokenHelper.getAccount());
        }
        Optional<Dictionary> dictionaryOptional = dictionaryRepository
                .findByAccountAndKey(accountHash, key);
        if (dictionaryOptional.isPresent()) {
            Dictionary dictionary = dictionaryOptional.get();
            dictionary.setValue(value);
            dictionary.setAccount(accountHash);
            return DictionaryMapper.INSTANCE.entityToResponse(dictionaryRepository.save(dictionary));
        }
        Dictionary dictionary = new Dictionary();
        dictionary.setKey(key);
        dictionary.setValue(value);
        dictionary.setAccount(accountHash);
        return DictionaryMapper.INSTANCE.entityToResponse(dictionaryRepository.save(dictionary));
    }

    public DictionaryResponse query(String key) {
        String accountHash = DigestUtils.md5Hex(TokenHelper.getAccount());
        Dictionary dictionary = dictionaryRepository.findByAccountAndKey(accountHash, key)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.DICTIONARY, key));
        return DictionaryMapper.INSTANCE.entityToResponse(dictionary);
    }

}
