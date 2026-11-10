package com.light.microservices.core.product.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.light.api.core.product.Product;
import com.light.api.exceptions.InvalidInputException;
import com.light.api.exceptions.NotFoundException;
import com.light.microservices.core.product.persistence.ProductEntity;
import com.light.microservices.core.product.persistence.ProductRepository;
import com.light.util.http.ServiceUtil;
import java.util.Arrays;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

/**
 * Unit tests for {@link ProductServiceImpl}, with the repository, mapper and
 * {@link ServiceUtil} mocked. Covers create (incl. duplicate-key translation),
 * the productId validation / not-found paths on get, the list mappings and delete.
 */
class ProductServiceImplTest {

    private ProductRepository repository;
    private ProductMapper mapper;
    private ServiceUtil serviceUtil;
    private ProductServiceImpl service;

    private Product api(int id) {
        return new Product(id, "name", 1, "brand", "cat", "img", "desc", 4, 9.0f, 5, null);
    }

    @BeforeEach
    void setup() {
        repository = mock(ProductRepository.class);
        mapper = mock(ProductMapper.class);
        serviceUtil = mock(ServiceUtil.class);
        when(serviceUtil.getServiceAddress()).thenReturn("product-addr");
        service = new ProductServiceImpl(repository, mapper, serviceUtil);
    }

    @Test
    void createProductPersistsAndReturnsApi() {
        ProductEntity entity = new ProductEntity(1, "name", 1);
        Product result = api(1);
        when(mapper.apiToEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.entityToApi(entity)).thenReturn(result);

        assertSame(result, service.createProduct(api(1)));
    }

    @Test
    void createProductTranslatesDuplicateKeyToInvalidInput() {
        when(mapper.apiToEntity(any())).thenReturn(new ProductEntity(1, "name", 1));
        when(repository.save(any())).thenThrow(new DuplicateKeyException("dup"));

        assertThrows(InvalidInputException.class, () -> service.createProduct(api(1)));
    }

    @Test
    void getProductRejectsNonPositiveId() {
        assertThrows(InvalidInputException.class, () -> service.getProduct(0));
    }

    @Test
    void getProductThrowsNotFoundWhenAbsent() {
        when(repository.findByProductId(13)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.getProduct(13));
    }

    @Test
    void getProductReturnsApiWithServiceAddress() {
        ProductEntity entity = new ProductEntity(1, "name", 1);
        when(repository.findByProductId(1)).thenReturn(Optional.of(entity));
        when(mapper.entityToApi(entity)).thenReturn(api(1));

        Product result = service.getProduct(1);

        assertEquals(1, result.getProductId());
        assertEquals("product-addr", result.getServiceAddress());
    }

    @Test
    void getAllProductsMapsAndSetsAddress() {
        ProductEntity e1 = new ProductEntity(1, "a", 1);
        ProductEntity e2 = new ProductEntity(2, "b", 1);
        when(repository.findAll()).thenReturn(Arrays.asList(e1, e2));
        when(mapper.entityToApi(e1)).thenReturn(api(1));
        when(mapper.entityToApi(e2)).thenReturn(api(2));

        assertEquals(2, service.getAllProducts().size());
        assertEquals("product-addr", service.getAllProducts().get(0).getServiceAddress());
    }

    @Test
    void searchByNameMapsResults() {
        ProductEntity e1 = new ProductEntity(1, "phone", 1);
        when(repository.findByNameContainingIgnoreCase("ph")).thenReturn(Arrays.asList(e1));
        when(mapper.entityToApi(e1)).thenReturn(api(1));

        assertEquals(1, service.searchByName("ph").size());
    }

    @Test
    void deleteProductDeletesWhenPresent() {
        ProductEntity entity = new ProductEntity(7, "x", 1);
        when(repository.findByProductId(7)).thenReturn(Optional.of(entity));

        service.deleteProduct(7);

        verify(repository).delete(entity);
    }

    @Test
    void deleteProductIsNoOpWhenAbsent() {
        when(repository.findByProductId(7)).thenReturn(Optional.empty());

        service.deleteProduct(7);

        verify(repository, never()).delete(any());
    }
}
