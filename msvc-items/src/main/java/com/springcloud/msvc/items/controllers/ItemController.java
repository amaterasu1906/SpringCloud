package com.springcloud.msvc.items.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.springcloud.msvc.items.models.Item;
import com.springcloud.msvc.items.models.Product;
import com.springcloud.msvc.items.services.ItemService;

import java.time.LocalDate;
import java.util.List;
import java.util.Collections;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class ItemController {
    private final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService service;
    private final CircuitBreakerFactory cBreakerFactory;

    public ItemController(ItemService service, CircuitBreakerFactory cBreakerFactory) {
        this.service = service;
        this.cBreakerFactory = cBreakerFactory;
    }

    @GetMapping
    public List<Item> list(@RequestParam(name = "name", required = false) String name, @RequestHeader(name = "token-request", required = false) String token) {
        logger.info(name);
        logger.info(token);
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> details(@PathVariable Long id) {
        Optional<Item> itemOptional = cBreakerFactory.create("items").run( () -> service.findById(id), e-> {
            Product product = new Product();
            product.setCreateAt(LocalDate.now());
            product.setId(1L);
            product.setName("Camara Sony");
            product.setPrice(500.00);
            logger.error(e.getMessage());
            return Optional.of(new Item(product, 5));
        });
        if (itemOptional.isPresent())
            return ResponseEntity.ok(itemOptional.get());
        return ResponseEntity.status(404).body(Collections.singletonMap("messages", "Id not Found"));
    }

}
