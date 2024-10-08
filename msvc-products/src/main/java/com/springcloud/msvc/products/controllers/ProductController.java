package com.springcloud.msvc.products.controllers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.springcloud.msvc.products.entities.Product;
import com.springcloud.msvc.products.services.ProductService;

@RestController
public class ProductController {
    final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<Product> list() {
        return this.productService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> details(@PathVariable Long id) throws InterruptedException {
        if(id.equals(12L)){
            throw new IllegalStateException("Producto no encontrado!");
        }
        if(id.equals(15L)){
            TimeUnit.SECONDS.sleep(2L);
        }

        Optional<Product> productOptinal = this.productService.findById(id);
        if (productOptinal.isPresent())
            return ResponseEntity.ok(productOptinal.orElseThrow());
        return ResponseEntity.notFound().build();
    }

}
