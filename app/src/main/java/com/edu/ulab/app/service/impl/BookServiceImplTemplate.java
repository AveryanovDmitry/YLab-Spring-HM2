package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class BookServiceImplTemplate implements BookService {
    private static final String INSERT_SQL = "INSERT INTO BOOK(TITLE, AUTHOR, PAGE_COUNT, USER_ID) VALUES (?,?,?,?)";
    private static final String SQL_UPDATE_BOOK = "UPDATE BOOK SET TITLE = ?, AUTHOR = ?, PAGE_COUNT = ? WHERE ID = ?";
    private static final String SQL_GET_BOOKS_BY_USERID = "SELECT * FROM BOOK WHERE USER_ID = ?";

    private static final String SQL_GET_BOOK_BY_ID = "SELECT * FROM BOOK WHERE ID = ?";

    private static final String SQL_DELETE_BOOK_BY_ID = "DELETE FROM BOOK WHERE ID = ?";
    private static final String SQL_DELETE_BOOK_BY_USERID = "DELETE FROM BOOK WHERE USER_ID = ?";

    private final JdbcTemplate jdbcTemplate;

    BookMapper mapper;

    public BookServiceImplTemplate(JdbcTemplate jdbcTemplate, BookMapper mapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapper = mapper;
    }

    @Override
    public BookDto createBook(BookDto bookDto) {

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                        PreparedStatement ps =
                                connection.prepareStatement(INSERT_SQL, new String[]{"id"});
                        ps.setString(1, bookDto.getTitle());
                        ps.setString(2, bookDto.getAuthor());
                        ps.setLong(3, bookDto.getPageCount());
                        ps.setLong(4, bookDto.getUserId());
                        return ps;
                    }
                },
                keyHolder);

        bookDto.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return bookDto;
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        if (jdbcTemplate.update(SQL_UPDATE_BOOK,
                bookDto.getTitle(), bookDto.getAuthor(), bookDto.getPageCount(), bookDto.getId()) == 0) {
            throw new NotFoundException("нет книги с id: " + bookDto.getId());
        }
        log.info("Update book: {}", bookDto);
        return bookDto;
    }

    @Override
    public BookDto getBookById(Long id) {
        log.info("find book by id: " + id);
        Book book = jdbcTemplate.query(SQL_GET_BOOK_BY_ID, new BeanPropertyRowMapper<>(Book.class), id)
                .stream().findAny().orElse(null);

        log.info("Found book: " + book);
        return mapper.bookToBookDto(book);
    }

    public List<BookDto> getBooksListByUserId(Long userId) {
        List<Book> books = jdbcTemplate.query(SQL_GET_BOOKS_BY_USERID,
                new BeanPropertyRowMapper<>(Book.class), userId);

        return books.stream()
                .filter(Objects::nonNull)
                .map(book -> mapper.bookToBookDto(book))
                .toList();
    }

    @Override
    public void deleteBookById(Long id) {
        if (jdbcTemplate.update(SQL_DELETE_BOOK_BY_ID, id) == 0) {
            throw new NotFoundException("нет книги с id: " + id);
        }
    }

    public void deleteBooksByUserId(Long userId) {
        if (jdbcTemplate.update(SQL_DELETE_BOOK_BY_USERID, userId) == 0) {
            throw new NotFoundException("нет книги с userId: " + userId);
        }
    }
}
