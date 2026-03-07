package com.food.ordering.users.infrastructure.adapter.output.persistence;

import com.food.ordering.users.application.port.output.UserRepositoryPort;
import com.food.ordering.users.domain.model.User;
import com.food.ordering.users.infrastructure.adapter.output.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserPersistenceAdapter(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = userMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }
}
