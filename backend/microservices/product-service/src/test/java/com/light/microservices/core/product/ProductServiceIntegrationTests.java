package com.light.microservices.core.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.light.api.core.product.Product;
import com.light.api.core.product.ProductService;
import com.light.api.exceptions.InvalidInputException;
import com.light.api.exceptions.NotFoundException;
import com.light.microservices.core.product.persistence.ProductRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests driving the real {@code ProductServiceImpl} against a MongoDB
 * Testcontainer. Unlike the mock-based unit tests, these exercise the generated
 * MapStruct {@code ProductMapperImpl}, the {@code ProductEntity} and the real
 * repository (incl. the unique-index duplicate-key path and the name search query).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class ProductServiceIntegrationTests extends MongoDbTestBase {

    @Autowired
    private ProductService service;

    @Autowired
    private ProductRepository repository;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    private Product product(int id, String name) {
        return new Product(id, name, 1, "Acme", "Tools", "img", "desc", 4, 9.0f, 5, null);
    }

    @Test
    void createAndGetProductRoundTrip() {
        service.createProduct(product(1, "Widget"));

        Product found = service.getProduct(1);
        assertEquals(1, found.getProductId());
        assertEquals("Widget", found.getName());
        assertEquals("Acme", found.getBrand());        // mapped real field
        assertEquals(9.0f, found.getPrice());
        assertNotNull(found.getServiceAddress());       // decorated by ServiceUtil
        assertEquals(1, repository.count());
    }

    @Test
    void duplicateProductIdThrowsInvalidInput() {
        service.createProduct(product(1, "Widget"));
        assertThrows(InvalidInputException.class, () -> service.createProduct(product(1, "Duplicate")));
    }

    @Test
    void getAllProductsReturnsEveryStoredProduct() {
        service.createProduct(product(1, "Phone"));
        service.createProduct(product(2, "Phone Case"));
        service.createProduct(product(3, "Laptop"));

        assertEquals(3, service.getAllProducts().size());
    }

    @Test
    void searchByNameMatchesCaseInsensitiveSubstring() {
        service.createProduct(product(1, "Phone"));
        service.createProduct(product(2, "Phone Case"));
        service.createProduct(product(3, "Laptop"));

        List<Product> phones = service.searchByName("phone");
        assertEquals(2, phones.size());
    }

    @Test
    void deleteProductRemovesItFromDatabase() {
        service.createProduct(product(1, "Widget"));

        service.deleteProduct(1);

        assertEquals(0, repository.count());
        assertThrows(NotFoundException.class, () -> service.getProduct(1));
    }

    @Test
    void getInvalidIdThrowsInvalidInput() {
        assertThrows(InvalidInputException.class, () -> service.getProduct(0));
    }

    @Test
    void getMissingProductThrowsNotFound() {
        assertThrows(NotFoundException.class, () -> service.getProduct(404));
    }
}
