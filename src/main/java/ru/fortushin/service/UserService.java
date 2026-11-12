package ru.fortushin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.fortushin.entity.Profile;
import ru.fortushin.entity.User;
import ru.fortushin.entity.enums.Role;
import ru.fortushin.exception.BusinessException;
import ru.fortushin.exception.NotFoundException;
import ru.fortushin.repository.UserRepository;
import ru.fortushin.web.dto.DtoMapper;
import ru.fortushin.web.dto.ProfileDto;
import ru.fortushin.web.dto.UserDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User create(String name, String email, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("User with email " + email + " already exists");
        }
        return userRepository.save(User.builder().name(name).email(email).role(role).build());
    }

    @Transactional
    public Profile upsertProfile(Long userId, String bio, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User", userId));
        Profile profile = user.getProfile();
        if (profile == null) {
            profile = Profile.builder().bio(bio).avatarUrl(avatarUrl).build();
            user.setProfile(profile);
        } else {
            profile.setBio(bio);
            profile.setAvatarUrl(avatarUrl);
        }
        return profile;
    }

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    public User getByIdWithProfile(Long id) {
        return userRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new NotFoundException("User", id));
    }

    public List<User> findAll() { return userRepository.findAll(); }
    public List<User> findByRole(Role role) { return userRepository.findAllByRole(role); }
    @Transactional(readOnly = true)
    public List<UserDto> listDto() {
        return userRepository.findAll().stream()
                .map(u -> userRepository.findByIdWithProfile(u.getId())
                        .map(DtoMapper::toDto)
                        .orElseGet(() -> DtoMapper.toDto(u)))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getDto(Long id) {
        User u = userRepository.findByIdWithProfile(id)
                .orElseThrow(() -> new NotFoundException("User", id));
        return DtoMapper.toDto(u);
    }

    @Transactional
    public UserDto createDto(String name, String email, Role role) {
        User saved = create(name, email, role);
        return DtoMapper.toDto(saved);
    }

    @Transactional
    public ProfileDto upsertProfileDto(Long userId, String bio, String avatarUrl) {
        Profile p = upsertProfile(userId, bio, avatarUrl);
        return DtoMapper.toDto(p);
    }
}