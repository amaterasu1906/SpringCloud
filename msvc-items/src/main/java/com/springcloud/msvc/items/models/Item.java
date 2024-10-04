package com.springcloud.msvc.items.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Item {
    private Product product;
    private Integer quantity;

    public Item() {
    }

    public Item(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Double getTotal() {
        return this.product.getPrice() * this.quantity;
    }
}
