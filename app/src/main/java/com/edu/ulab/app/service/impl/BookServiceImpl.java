package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.repository.BookRepository;
import com.edu.ulab.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository,
                           BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public BookDto createBook(BookDto bookDto) {
        Book book = bookMapper.bookDtoToBook(bookDto);
        log.info("Mapped book: {}", book);
        Book savedBook = bookRepository.save(book);
        log.info("Saved book: {}", savedBook);
        return bookMapper.bookToBookDto(savedBook);
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        if (!bookRepository.existsById(bookDto.getId())) {
            throw new NotFoundException("Книги с таким id не найдено");
        }
        Book book = bookMapper.bookDtoToBook(bookDto);
        log.info("Book to update: {}", book);
        Book updatedBook = bookRepository.save(book);
        log.info("Saved book: {}", updatedBook);
        return bookMapper.bookToBookDto(updatedBook);
    }

    @Override
    public BookDto getBookById(Long id) {
        log.info("Get book by id: " + id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book no found by id: " + id));
        return bookMapper.bookToBookDto(book);
    }

    public List<BookDto> getBooksListByUserId(Long useId) {
        log.info("Get list book by user id: " + useId);
        List<Book> books = (List<Book>) bookRepository.findAllById(Collections.singleton(useId));
        return books.stream().map(bookMapper::bookToBookDto).toList();
    }

    @Override
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
        log.info("Book delete by id: {}", id);
    }

    public void deleteBooksByUserId(Long userId) {
        bookRepository.deleteAllBooksByUserId(userId);
    }
}
