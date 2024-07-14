package com.bookstore.bookstore.Controller;

import com.bookstore.bookstore.Repository.CategoryRepository;
import com.bookstore.bookstore.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@ComponentScan(basePackages = "com.bookstore.bookstore.Services")
@Configuration
@RestController
@RequestMapping("api")
public class CategoryController {

    @Autowired
    private final CategoryRepository categoryRepository;

    public CategoryController(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @GetMapping("/category/list")
    public Map<String, Object> List(
            @RequestHeader("token") String Token,
            @RequestParam("report") String report
    ) throws IOException {
        return  categoryRepository.getCategory(Token, report);
    }

}
