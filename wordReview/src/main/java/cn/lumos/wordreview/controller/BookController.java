package cn.lumos.wordreview.controller;

import cn.lumos.wordreview.dto.Result;
import cn.lumos.wordreview.service.IBookService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/books")
public class BookController {
    @Resource
    private IBookService bookService;
    @GetMapping("/{id}")
    public Result getPGBook(@PathVariable Long id) {
        return bookService.getWordsForBook(id);
    }
}
