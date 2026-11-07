package io.yltoggle.api.service;

import io.yltoggle.api.dto.TagRequest;
import io.yltoggle.api.dto.TagResponse;
import io.yltoggle.api.dao.entity.Tag;
import io.yltoggle.api.mapper.TagMapper;
import io.yltoggle.api.dao.repository.TagRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class TagService {

    private TagRepository tagRepository;

    @PersistenceContext
    public EntityManager entityManager;

    public List<TagResponse> queryByProjectKey(String projectKey) {
        List<Tag> tags = tagRepository.findByProjectKey(projectKey);
        return tags.stream().map(TagMapper.INSTANCE::entityToResponse).collect(Collectors.toList());
    }

    public TagResponse create(String projectKey, TagRequest tagRequest) {
        Tag tag = tagRepository.findByProjectKeyAndName(projectKey, tagRequest.getName());
        if (tag == null) {
            tag = TagMapper.INSTANCE.requestToEntity(tagRequest);
            tag.setProjectKey(projectKey);
            tag.setDeleted(false);
            tagRepository.save(tag);
        }
        return TagMapper.INSTANCE.entityToResponse(tag);
    }
}
