package com.light.api.composite.product;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "ProductComposite", description = "REST API for composite product information.")
public interface ProductCompositeService {

    /**
     * Sample usage, see below.
     *
     * curl -X POST $HOST:$PORT/product-composite \
     *   -H "Content-Type: application/json" --data \
     *   '{"productId":123,"name":"product 123","weight":123}'
     *
     * @param body A JSON representation of the new composite product
     */
    @Operation(
            summary = "${api.product-composite.create-composite-product.description}",
            description = "${api.product-composite.create-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @PostMapping(
            value    = "/product-composite",
            consumes = "application/json")
    void createProduct(@RequestBody ProductAggregate body);

    /**
     * Sample usage: "curl $HOST:$PORT/product-composite/1".
     *
     * @param productId Id of the product
     * @return the composite product info, if found, else null
     */
    @Operation(
            summary = "${api.product-composite.get-composite-product.description}",
            description = "${api.product-composite.get-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "404", description = "${api.responseCodes.notFound.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @GetMapping(
            value = "/product-composite/{productId}",
            produces = "application/json")
    ProductAggregate getProduct(@PathVariable int productId);

    /**
     * Sample usage: "curl $HOST:$PORT/product-composite".
     *
     * @return a list of all composite products
     */
    @Operation(
            summary = "Returns a list of all products",
            description = "Returns a composite view of all available products.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}")
    })
    @GetMapping(
            value = "/product-composite",
            produces = "application/json")
    List<ProductAggregate> getAllProducts();

    /**
     * Sample usage: "curl $HOST:$PORT/product-composite/search?name=phone".
     *
     * @param name (partial, case-insensitive) product name to search for
     * @return a list of matching composite products
     */
    @Operation(
            summary = "Searches products by name",
            description = "Returns a composite view of products whose name contains the given text (case-insensitive).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}")
    })
    @GetMapping(
            value = "/product-composite/search",
            produces = "application/json")
    List<ProductAggregate> searchProducts(@RequestParam(value = "name") String name);

    /**
     * Sample usage: "curl -X POST $HOST:$PORT/product-composite/1/review ...".
     *
     * @param productId Id of the product the review belongs to
     * @param body      the review to add (reviewId is assigned by the service)
     */
    @Operation(
            summary = "Adds a review to an existing product",
            description = "Creates a review for the given productId. The reviewId is generated automatically.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @PostMapping(
            value = "/product-composite/{productId}/review",
            consumes = "application/json")
    void addReview(@PathVariable int productId, @RequestBody ReviewSummary body);

    /**
     * Sample usage: "curl -X POST $HOST:$PORT/product-composite/1/recommendation ...".
     *
     * @param productId Id of the product the recommendation belongs to
     * @param body      the recommendation to add (recommendationId is assigned by the service)
     */
    @Operation(
            summary = "Adds a recommendation to an existing product",
            description = "Creates a recommendation for the given productId. The recommendationId is generated automatically.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @PostMapping(
            value = "/product-composite/{productId}/recommendation",
            consumes = "application/json")
    void addRecommendation(@PathVariable int productId, @RequestBody RecommendationSummary body);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product-composite/1/review/2".
     *
     * @param productId Id of the product
     * @param reviewId  Id of the review to delete
     */
    @Operation(
            summary = "Deletes a single review from a product",
            description = "Deletes the review with the given reviewId from the given productId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}")
    })
    @DeleteMapping(value = "/product-composite/{productId}/review/{reviewId}")
    void deleteReview(@PathVariable int productId, @PathVariable int reviewId);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product-composite/1/recommendation/2".
     *
     * @param productId        Id of the product
     * @param recommendationId Id of the recommendation to delete
     */
    @Operation(
            summary = "Deletes a single recommendation from a product",
            description = "Deletes the recommendation with the given recommendationId from the given productId.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "${api.responseCodes.ok.description}")
    })
    @DeleteMapping(value = "/product-composite/{productId}/recommendation/{recommendationId}")
    void deleteRecommendation(@PathVariable int productId, @PathVariable int recommendationId);

    /**
     * Sample usage: "curl -X DELETE $HOST:$PORT/product-composite/1".
     *
     * @param productId Id of the product
     */
    @Operation(
            summary = "${api.product-composite.delete-composite-product.description}",
            description = "${api.product-composite.delete-composite-product.notes}")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "${api.responseCodes.badRequest.description}"),
            @ApiResponse(responseCode = "422", description = "${api.responseCodes.unprocessableEntity.description}")
    })
    @DeleteMapping(value = "/product-composite/{productId}")
    void deleteProduct(@PathVariable int productId);
}