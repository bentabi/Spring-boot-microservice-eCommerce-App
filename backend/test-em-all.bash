#!/usr/bin/env bash
#
# End-to-end regression test for the product-composite stack.
# Exercises products, search, reviews, recommendations (add/delete) against the
# running composite service and asserts HTTP status codes and response content.
#
# Usage:   ./test-em-all.bash              (defaults to localhost:8080)
#          HOST=localhost PORT=8080 ./test-em-all.bash
#
set -u

: ${HOST=localhost}
: ${PORT=8080}
: ${PROD_ID=999}

BASE="http://$HOST:$PORT/product-composite"
FAILED=0
PASSED=0

# assertCurl <expectedHttpCode> <curl command without -w>
function assertCurl() {
  local expected=$1
  local curlCmd="$2 -w \"%{http_code}\""
  local result
  result=$(eval $curlCmd)
  local httpCode="${result:(-3)}"
  RESPONSE=""
  if [ ${#result} -gt 3 ]; then RESPONSE="${result%???}"; fi

  if [ "$httpCode" = "$expected" ]; then
    echo "PASS: $expected for [$2]"
    PASSED=$((PASSED+1))
  else
    echo "FAIL: expected $expected, got $httpCode for [$2]"
    echo "      body: $RESPONSE"
    FAILED=$((FAILED+1))
  fi
}

# assertContains <description> <needle> ; checks the last RESPONSE
function assertContains() {
  if echo "$RESPONSE" | grep -q "$2"; then
    echo "PASS: response contains [$2] ($1)"
    PASSED=$((PASSED+1))
  else
    echo "FAIL: response missing [$2] ($1)"
    echo "      body: $RESPONSE"
    FAILED=$((FAILED+1))
  fi
}

# assertNotContains <description> <needle>
function assertNotContains() {
  if echo "$RESPONSE" | grep -q "$2"; then
    echo "FAIL: response should NOT contain [$2] ($1)"
    echo "      body: $RESPONSE"
    FAILED=$((FAILED+1))
  else
    echo "PASS: response does not contain [$2] ($1)"
    PASSED=$((PASSED+1))
  fi
}

echo "=== Regression test against $BASE (productId=$PROD_ID) ==="

# 0. Clean any leftover from a previous run
curl -s -X DELETE "$BASE/$PROD_ID" > /dev/null

# 1. Create the product
echo "--- create product ---"
BODY="{\"productId\":$PROD_ID,\"name\":\"Regression Widget\",\"weight\":3,\"brand\":\"Acme\",\"category\":\"Tools\",\"price\":42.5,\"countInStock\":9,\"description\":\"regression test product\",\"image\":\"\",\"rating\":0}"
assertCurl 200 "curl -s -X POST $BASE -H \"Content-Type: application/json\" -d '$BODY'"

# 2. Get it back and verify the REAL fields are returned (Task 3 fix)
echo "--- get product (verify real fields, not hardcoded) ---"
assertCurl 200 "curl -s $BASE/$PROD_ID"
assertContains "brand"    '"brand":"Acme"'
assertContains "category" '"category":"Tools"'
assertContains "price"    '"price":42.5'

# 3. List all -> should include our product
echo "--- list all ---"
assertCurl 200 "curl -s $BASE"
assertContains "list has product" "\"productId\":$PROD_ID"

# 4. Search by name -> should find it
echo "--- search by name ---"
assertCurl 200 "curl -s \"$BASE/search?name=widget\""
assertContains "search match" "Regression Widget"

# 5. Add a review, then verify it comes back on the aggregate
echo "--- add review ---"
assertCurl 200 "curl -s -X POST $BASE/$PROD_ID/review -H \"Content-Type: application/json\" -d '{\"author\":\"Tester\",\"subject\":\"Works\",\"content\":\"All good\"}'"
assertCurl 200 "curl -s $BASE/$PROD_ID"
assertContains "review present" '"subject":"Works"'

# 6. Add a recommendation, verify
echo "--- add recommendation ---"
assertCurl 200 "curl -s -X POST $BASE/$PROD_ID/recommendation -H \"Content-Type: application/json\" -d '{\"author\":\"Tester\",\"rate\":5,\"content\":\"Recommended\"}'"
assertCurl 200 "curl -s $BASE/$PROD_ID"
assertContains "recommendation present" '"content":"Recommended"'

# 7. Delete the review (id 1) and verify it's gone
echo "--- delete review ---"
assertCurl 200 "curl -s -X DELETE $BASE/$PROD_ID/review/1"
assertCurl 200 "curl -s $BASE/$PROD_ID"
assertNotContains "review removed" '"subject":"Works"'

# 8. Delete the recommendation (id 1) and verify it's gone
echo "--- delete recommendation ---"
assertCurl 200 "curl -s -X DELETE $BASE/$PROD_ID/recommendation/1"
assertCurl 200 "curl -s $BASE/$PROD_ID"
assertNotContains "recommendation removed" '"content":"Recommended"'

# 9. Delete the product -> subsequent GET should be 404
echo "--- delete product ---"
assertCurl 200 "curl -s -X DELETE $BASE/$PROD_ID"
assertCurl 404 "curl -s $BASE/$PROD_ID"

echo ""
echo "================ SUMMARY ================"
echo "PASSED: $PASSED"
echo "FAILED: $FAILED"
if [ $FAILED -ne 0 ]; then
  echo "RESULT: REGRESSION FAILED"
  exit 1
fi
echo "RESULT: ALL TESTS PASSED"
exit 0
