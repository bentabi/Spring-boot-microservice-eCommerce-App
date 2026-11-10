package com.light.microservices.core.recommendation.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.light.api.core.recommendation.Recommendation;
import com.light.api.exceptions.InvalidInputException;
import com.light.microservices.core.recommendation.persistence.RecommendationEntity;
import com.light.microservices.core.recommendation.persistence.RecommendationRepository;
import com.light.util.http.ServiceUtil;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;

/**
 * Unit tests for {@link RecommendationServiceImpl} with mocked repository, mapper
 * and {@link ServiceUtil}. Covers create (incl. duplicate-key translation), the
 * productId validation on get, the address-decorated list mapping and the two
 * delete variants (bulk vs. single, the latter filtering by recommendationId).
 */
class RecommendationServiceImplTest {

    private RecommendationRepository repository;
    private RecommendationMapper mapper;
    private ServiceUtil serviceUtil;
    private RecommendationServiceImpl service;

    private Recommendation api(int recId) {
        return new Recommendation(1, recId, "author", 5, "content", null);
    }

    @BeforeEach
    void setup() {
        repository = mock(RecommendationRepository.class);
        mapper = mock(RecommendationMapper.class);
        serviceUtil = mock(ServiceUtil.class);
        when(serviceUtil.getServiceAddress()).thenReturn("rec-addr");
        service = new RecommendationServiceImpl(repository, mapper, serviceUtil);
    }

    @Test
    void createRecommendationPersistsAndReturnsApi() {
        RecommendationEntity entity = new RecommendationEntity(1, 1, "a", 5, "c");
        Recommendation result = api(1);
        when(mapper.apiToEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.entityToApi(entity)).thenReturn(result);

        assertSame(result, service.createRecommendation(api(1)));
    }

    @Test
    void createRecommendationTranslatesDuplicateKeyToInvalidInput() {
        when(mapper.apiToEntity(any())).thenReturn(new RecommendationEntity(1, 1, "a", 5, "c"));
        when(repository.save(any())).thenThrow(new DuplicateKeyException("dup"));

        assertThrows(InvalidInputException.class, () -> service.createRecommendation(api(1)));
    }

    @Test
    void getRecommendationsRejectsNonPositiveId() {
        assertThrows(InvalidInputException.class, () -> service.getRecommendations(0));
    }

    @Test
    void getRecommendationsReturnsListWithServiceAddress() {
        RecommendationEntity entity = new RecommendationEntity(1, 1, "a", 5, "c");
        when(repository.findByProductId(1)).thenReturn(Arrays.asList(entity));
        List<Recommendation> mapped = Arrays.asList(api(1));
        when(mapper.entityListToApiList(any())).thenReturn(mapped);

        List<Recommendation> result = service.getRecommendations(1);

        assertEquals(1, result.size());
        assertEquals("rec-addr", result.get(0).getServiceAddress());
    }

    @Test
    void deleteRecommendationsDeletesAllForProduct() {
        List<RecommendationEntity> entities = Arrays.asList(
                new RecommendationEntity(1, 1, "a", 5, "c"),
                new RecommendationEntity(1, 2, "a", 4, "c"));
        when(repository.findByProductId(1)).thenReturn(entities);

        service.deleteRecommendations(1);

        verify(repository).deleteAll(entities);
    }

    @Test
    void deleteRecommendationDeletesOnlyMatchingId() {
        RecommendationEntity keep = new RecommendationEntity(1, 1, "a", 5, "c");
        RecommendationEntity remove = new RecommendationEntity(1, 7, "a", 4, "c");
        when(repository.findByProductId(1)).thenReturn(Arrays.asList(keep, remove));

        service.deleteRecommendation(1, 7);

        verify(repository).delete(remove);
        verify(repository, never()).delete(keep);
    }
}
