package com.bookstore.bookstore.Controller;

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
public class ProductController {

    @Autowired
    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @GetMapping("/product/list")
    public Map<String, Object> List(
            @RequestHeader("token") String Token,
            @RequestParam("report") String report
    ) throws IOException {
        return  productRepository.getproduct(Token, report);
    }

    @GetMapping("/product/code-verify")
    public  Map<String, Object> ProductCode(
            @RequestHeader("token") String Token,
            @RequestParam("code") String Code
    ) throws IOException{
        return productRepository.getproductcode(Token, Code);
    }

    @PostMapping("/product/save")
    public Map<String, Object> Save(
            @RequestHeader("token") String Token,
            @RequestBody String report
    ) throws IOException {
        return  productRepository.getproduct(Token, report);
    }

    @PutMapping ("/product/edit")
    public Map<String, Object> Update(
            @RequestHeader("token") String Token,
            @RequestBody String report
    ) throws IOException {
        return  productRepository.getproduct(Token, report);
    }
}
