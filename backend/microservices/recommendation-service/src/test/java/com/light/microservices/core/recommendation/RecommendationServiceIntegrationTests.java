package com.light.microservices.core.recommendation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.light.api.core.recommendation.Recommendation;
import com.light.api.core.recommendation.RecommendationService;
import com.light.api.exceptions.InvalidInputException;
import com.light.microservices.core.recommendation.persistence.RecommendationRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration tests driving the real {@code RecommendationServiceImpl} against a
 * MongoDB Testcontainer. These exercise the generated MapStruct
 * {@code RecommendationMapperImpl} (incl. the rating&lt;-&gt;rate field mapping), the
 * entity, and the repository (compound unique index duplicate-key path).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class RecommendationServiceIntegrationTests extends MongoDbTestBase {

    @Autowired
    private RecommendationService service;

    @Autowired
    private RecommendationRepository repository;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    private Recommendation recommendation(int productId, int recId) {
        return new Recommendation(productId, recId, "author", 5, "content", null);
    }

    @Test
    void createAndGetRoundTripMapsRateField() {
        service.createRecommendation(recommendation(1, 1));

        List<Recommendation> recs = service.getRecommendations(1);
        assertEquals(1, recs.size());
        assertEquals(1, recs.get(0).getRecommendationId());
        assertEquals(5, recs.get(0).getRate());          // rating -> rate via mapper
        assertEquals("author", recs.get(0).getAuthor());
        assertNotNull(recs.get(0).getServiceAddress());
    }

    @Test
    void duplicateProductAndRecommendationIdThrowsInvalidInput() {
        service.createRecommendation(recommendation(1, 1));
        assertThrows(InvalidInputException.class, () -> service.createRecommendation(recommendation(1, 1)));
    }

    @Test
    void getRecommendationsRejectsNonPositiveId() {
        assertThrows(InvalidInputException.class, () -> service.getRecommendations(0));
    }

    @Test
    void getRecommendationsReturnsEmptyForUnknownProduct() {
        assertTrue(service.getRecommendations(999).isEmpty());
    }

    @Test
    void deleteRecommendationsRemovesAllForProduct() {
        service.createRecommendation(recommendation(1, 1));
        service.createRecommendation(recommendation(1, 2));

        service.deleteRecommendations(1);

        assertTrue(service.getRecommendations(1).isEmpty());
    }

    @Test
    void deleteRecommendationRemovesOnlyTheMatchingOne() {
        service.createRecommendation(recommendation(1, 1));
        service.createRecommendation(recommendation(1, 2));

        service.deleteRecommendation(1, 1);

        List<Recommendation> remaining = service.getRecommendations(1);
        assertEquals(1, remaining.size());
        assertEquals(2, remaining.get(0).getRecommendationId());
    }
}
