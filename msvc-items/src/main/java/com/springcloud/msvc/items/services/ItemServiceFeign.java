package com.springcloud.msvc.items.services;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.springcloud.msvc.items.clients.ProductFeignClient;
import com.springcloud.msvc.items.models.Item;
import com.springcloud.msvc.items.models.Product;

import feign.FeignException;

@Service
public class ItemServiceFeign implements ItemService {
    @Autowired
    private ProductFeignClient client;
    private Random random = new Random();

    @Override
    public List<Item> findAll() {
        return client.findAll().stream().map(product -> new Item(product, random.nextInt(10) + 1))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Item> findById(Long id) {
        try {
            Product product = client.details(id);
            return Optional.of(new Item(product, random.nextInt(10) + 1));
        } catch (FeignException e) {
            return Optional.empty();
        }
    }

    @Override
    public Product save(Product product) {
        return this.client.create(product);
    }

    @Override
    public Product update(Product product, Long id) {
        return this.client.update(product, id);
    }

    @Override
    public void delete(Long id) {
        this.client.delete(id);
    }

}
