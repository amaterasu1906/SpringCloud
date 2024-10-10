package com.springcloud.msvc.items.controllers;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.springcloud.msvc.items.models.Item;
import com.springcloud.msvc.items.models.Product;
import com.springcloud.msvc.items.services.ItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
@RefreshScope
@RestController
public class ItemController {
    private final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService service;
    private final CircuitBreakerFactory cBreakerFactory;

    @Value("${configuracion.texto}")
    private String text;

    @Autowired
    private Environment env;

    @GetMapping("/fetch-configs")
    public ResponseEntity<Object> fetchConfig(@Value("${server.port}") String port) {
        Map<String, String> json = new HashMap<>();
        json.put("text", text);
        json.put("port", port);
        logger.info(text);
        logger.info(port);
        if( env.getActiveProfiles().length > 0 && env.getActiveProfiles()[0].equalsIgnoreCase("dev")){
            json.put("autor.nombre", env.getProperty("configuracion.autor.nombre"));
            json.put("autor.email", env.getProperty("configuracion.autor.email"));
        }
        return ResponseEntity.ok(json);
    }
    

    public ItemController(@Qualifier("itemServiceFeign") ItemService service, CircuitBreakerFactory cBreakerFactory) {
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
    /*
     * Generamos el CircuitBreaker con la configuracion de items en application.yml
     * se llama a getFallBackMethodProduct para el camino alternativo
     */
    @CircuitBreaker(name="items", fallbackMethod = "getFallBackMethodProduct")
    @GetMapping("/details/{id}")
    public ResponseEntity<Object> detailsAnnotationCircuit(@PathVariable Long id) {
        Optional<Item> itemOptional = service.findById(id);
        if (itemOptional.isPresent())
            return ResponseEntity.ok(itemOptional.get());
        return ResponseEntity.status(404).body(Collections.singletonMap("messages", "Id not Found"));
    }

    /*
     * TimeLimiter - CompletableFuture permite calcular el tiempo de ejecucion de la funcion service.findById
     * para poder validar si entra en timeout
     * se llama a getFallBackMethodProduct2 para el camino alternativo
     */
    @CircuitBreaker(name="items", fallbackMethod="getFallBackMethodProduct2")
    @TimeLimiter(name="items")
    @GetMapping("/details2/{id}")
    public CompletableFuture<Object> detailsAnnotationTimeLimiter(@PathVariable Long id) {
        return CompletableFuture.supplyAsync( () -> { 
            Optional<Item> itemOptional = service.findById(id);
            if (itemOptional.isPresent())
                return ResponseEntity.ok(itemOptional.get());
            return ResponseEntity.status(404).body(Collections.singletonMap("messages", "Id not Found"));
        });
    }

    public CompletableFuture<Object> getFallBackMethodProduct2(Throwable e){
        return CompletableFuture.supplyAsync( () -> getFallBackMethodProduct(e));
    }

    public ResponseEntity<Object> getFallBackMethodProduct(Throwable e){
        logger.error(e.getMessage());
        Product product = new Product();
        product.setCreateAt(LocalDate.now());
        product.setId(1L);
        product.setName("Camara Sony");
        product.setPrice(500.00);
        return ResponseEntity.ok(new Item(product, 5));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Product create(@RequestBody Product product){
        return this.service.save(product);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Product update(@RequestBody Product product, @PathVariable Long id){
        return this.service.update(product, id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id){
        this.service.delete(id);
    }
}
