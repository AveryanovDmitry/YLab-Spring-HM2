package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImplTemplate implements UserService {
    private static final String INSERT_SQL = "INSERT INTO PERSON(FULL_NAME, TITLE, AGE) VALUES (?,?,?)";
    private static final String SQL_UPDATE_PERSON = "UPDATE PERSON SET FULL_NAME = ?, TITLE = ?, AGE  = ? WHERE ID = ?";
    private static final String SQL_GET_PERSON_BY_ID = "SELECT * FROM PERSON WHERE ID = ?";
    private static final String SQL_DELETE_PERSON_BY_ID = "DELETE FROM PERSON WHERE ID = ?";
    private final JdbcTemplate jdbcTemplate;

    private final UserMapper mapper;

    public UserServiceImplTemplate(JdbcTemplate jdbcTemplate, UserMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
                    ps.setString(1, userDto.getFullName());
                    ps.setString(2, userDto.getTitle());
                    ps.setLong(3, userDto.getAge());
                    return ps;
                }, keyHolder);

        userDto.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return userDto;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        if (jdbcTemplate.update(SQL_UPDATE_PERSON, userDto.getFullName(), userDto.getTitle(), userDto.getAge(),
                userDto.getId()) == 0) {
            throw new NotFoundException("Нет юзер с id: " + userDto.getId());
        }
        log.info("Update user: {}", userDto);
        return userDto;
    }

    @Override
    public UserDto getUserById(Long id) {
        log.info("Get user by id: " + id);
        Person foundUser = jdbcTemplate.query(SQL_GET_PERSON_BY_ID,
                        new BeanPropertyRowMapper<>(Person.class), id)
                .stream().findAny()
                .orElseThrow(() -> new NotFoundException("User with id " + id + "is not found"));

        log.info("Found user: {}", foundUser);
        return mapper.personToUserDto(foundUser);
    }

    @Override
    public void deleteUserById(Long id) {
        if (jdbcTemplate.update(SQL_DELETE_PERSON_BY_ID, id) == 0) {
            throw new NotFoundException("Не нашлось юзера с id: " + id);
        }
    }
}
