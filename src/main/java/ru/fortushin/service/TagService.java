package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.Tag;
import ru.fortushin.repository.TagRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> findAll() { return tagRepository.findAll(); }
}