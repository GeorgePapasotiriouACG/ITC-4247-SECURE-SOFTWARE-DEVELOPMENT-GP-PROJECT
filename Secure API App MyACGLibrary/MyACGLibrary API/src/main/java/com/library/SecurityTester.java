/*
 * SecurityTester - Automated Security Testing Tool
 * Created by: George Papasotiriou
 * Date: 2024-01-15
 *
 * Automated tool for testing REST API vulnerabilities including:
 * - SQL Injection
 * - Type Confusion
 * - Path Traversal
 * - Authorization Bypass
 * - Input Validation
 * - Extreme Input Handling
 */

package com.library;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

/**
 * Automated Security Testing Tool for REST APIs
 * This tool tests various vulnerabilities in web services
 */
public class SecurityTester {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static String authToken = "";
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    /**
     * Main method to run all security tests
     * Usage: java SecurityTester
     */
    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("    AUTOMATED API SECURITY TESTING TOOL");
        System.out.println("    Created by: George Papasotiriou");
        System.out.println("================================================");

        try {
            // Step 1: Test authentication
            if (!authenticate()) {
                System.out.println("\n⚠️  Trying alternative authentication method...");
                if (!registerAndLogin()) {
                    System.out.println("❌ All authentication attempts failed. Exiting.");
                    return;
                }
            }

            // Step 2: Run all vulnerability tests
            System.out.println("\n🚀 Starting Security Tests...");

            testSqlInjection();
            testTypeConfusion();
            testPathTraversal();
            testAuthorizationBypass();
            testInputValidation();
            testExtremeInputs();
            testMassAssignment();
            testErrorHandling();

            // NEW: Test PUT and DELETE endpoints
            testPutEndpoint();
            testDeleteEndpoint();

            System.out.println("\n================================================");
            System.out.println("          TESTING COMPLETE!");
            System.out.println("================================================");

        } catch (Exception e) {
            System.err.println("Test failed with error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Authenticate and get JWT token using admin credentials
     */
    private static boolean authenticate() throws IOException, InterruptedException {
        System.out.println("\n🔐 Testing Authentication...");

        // Test valid login with admin
        String json = "{\"username\":\"admin\",\"password\":\"password123\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("   Login attempt with admin/password123");
        System.out.println("   Status Code: " + response.statusCode());

        if (response.statusCode() == 200) {
            // Extract token from response
            String body = response.body();
            if (body.contains("\"token\":")) {
                int start = body.indexOf("\"token\":\"") + 9;
                int end = body.indexOf("\"", start);
                authToken = body.substring(start, end);
                System.out.println("✅ Authentication successful!");
                System.out.println("   Token obtained: " + authToken.substring(0, Math.min(20, authToken.length())) + "...");
                return true;
            }
        }

        System.out.println("❌ Authentication failed with admin. Response: " +
                response.body().substring(0, Math.min(100, response.body().length())));
        return false;
    }

    /**
     * Alternative: Register a new user and login
     */
    private static boolean registerAndLogin() throws IOException, InterruptedException {
        System.out.println("\n🔐 Attempting to register new user...");

        // Register a new user
        String registerJson = "{\"username\":\"tester\",\"password\":\"test123\",\"role\":\"USER\"}";

        HttpRequest registerRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/auth/register"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                .build();

        HttpResponse<String> registerResponse = client.send(registerRequest, HttpResponse.BodyHandlers.ofString());

        if (registerResponse.statusCode() == 200 || registerResponse.body().contains("success")) {
            System.out.println("✅ User registered successfully");

            // Now login with the new user
            String loginJson = "{\"username\":\"tester\",\"password\":\"test123\"}";

            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                    .build();

            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

            if (loginResponse.statusCode() == 200) {
                String body = loginResponse.body();
                if (body.contains("\"token\":")) {
                    int start = body.indexOf("\"token\":\"") + 9;
                    int end = body.indexOf("\"", start);
                    String userToken = body.substring(start, end);
                    authToken = userToken;  // Use the user token for testing
                    System.out.println("✅ Login with new user successful!");
                    System.out.println("   Token obtained: " + authToken.substring(0, Math.min(20, authToken.length())) + "...");
                    return true;
                }
            }
        }

        System.out.println("❌ Registration/login failed");
        return false;
    }

    /**
     * NEW: Test PUT endpoint with various inputs
     */
    private static void testPutEndpoint() {
        System.out.println("\n🔄 Testing PUT Endpoint (Update Book)...");

        // First, get a book ID to test with
        Long bookId = getFirstBookId();
        if (bookId == null) {
            System.out.println("   ⚠️  No books found to test PUT endpoint");
            return;
        }

        List<Map<String, Object>> testCases = new ArrayList<>();

        // Test case 1: Normal update
        Map<String, Object> case1 = new HashMap<>();
        case1.put("title", "Updated Title");
        case1.put("author", "Updated Author");
        case1.put("isbn", "999-999-999");
        case1.put("available", true);
        testCases.add(case1);

        // Test case 2: XSS and SQL injection in fields
        Map<String, Object> case2 = new HashMap<>();
        case2.put("title", "<script>alert('XSS')</script>");
        case2.put("author", "Author'); DROP TABLE books; --");
        case2.put("isbn", "123' OR '1'='1");
        testCases.add(case2);

        // Test case 3: Extremely long values
        Map<String, Object> case3 = new HashMap<>();
        case3.put("title", "A".repeat(1000));
        case3.put("author", "B".repeat(1000));
        case3.put("isbn", "C".repeat(100));
        testCases.add(case3);

        // Test case 4: Invalid data types
        Map<String, Object> case4 = new HashMap<>();
        case4.put("title", 12345);  // Number instead of string
        case4.put("author", true);  // Boolean instead of string
        case4.put("available", "not_a_boolean");  // String instead of boolean
        testCases.add(case4);

        // Test case 5: Empty fields
        Map<String, Object> case5 = new HashMap<>();
        case5.put("title", "");
        case5.put("author", "");
        case5.put("isbn", "");
        testCases.add(case5);

        for (int i = 0; i < testCases.size(); i++) {
            Map<String, Object> testCase = testCases.get(i);
            try {
                String json = buildJson(testCase);

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/books/" + bookId))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Test case " + (i + 1) + ": " + testCase);
                System.out.println("   Status: " + response.statusCode());

                if (response.statusCode() == 403) {
                    System.out.println("   ⚠️  Authorization required (admin only)");
                } else {
                    System.out.println("   Response: " + response.body().substring(0, Math.min(100, response.body().length())));
                }

                Thread.sleep(200);

            } catch (Exception e) {
                System.out.println("   Error with test case " + (i + 1) + ": " + e.getMessage());
            }
        }

        // Test type confusion in path parameter
        testPutTypeConfusion();
    }

    /**
     * NEW: Test PUT endpoint with type confusion in ID parameter
     */
    private static void testPutTypeConfusion() {
        System.out.println("\n   🔢 Testing PUT Type Confusion (Invalid IDs)...");

        List<String> invalidIds = Arrays.asList(
                "abc",          // String instead of number
                "-1",           // Negative number
                "0",            // Zero (likely invalid)
                "999999999999999999999999999999",  // Very large number
                "1.5",          // Float instead of integer
                "1; DROP TABLE books; --",  // SQL in ID
                "<script>alert(1)</script>",  // XSS in ID
                "null",         // null string
                "true",         // boolean
                "' OR '1'='1"   // SQL injection in ID
        );

        for (String id : invalidIds) {
            try {
                String json = "{\"title\":\"Test\",\"author\":\"Test\",\"isbn\":\"123\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/books/" + id))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("     ID: " + id);
                System.out.println("     Status: " + response.statusCode());

                // Check for error messages that reveal too much information
                String body = response.body();
                if (body.contains("Exception") || body.contains("SQL") ||
                        body.contains("syntax") || body.contains("NumberFormatException")) {
                    System.out.println("     ⚠️  INFORMATION DISCLOSURE: " +
                            body.substring(0, Math.min(80, body.length())).replace("\n", " "));
                }

                Thread.sleep(100);

            } catch (Exception e) {
                System.out.println("     Error with ID '" + id + "': " + e.getMessage());
            }
        }
    }

    /**
     * NEW: Test DELETE endpoint with various inputs
     */
    private static void testDeleteEndpoint() {
        System.out.println("\n🗑️  Testing DELETE Endpoint...");

        // First, create a test book to delete
        Long testBookId = createTestBook();
        if (testBookId == null) {
            System.out.println("   ⚠️  Could not create test book for DELETE testing");
            return;
        }

        // Test 1: Normal delete
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/books/" + testBookId))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("   Test 1: Normal delete of book " + testBookId);
            System.out.println("   Status: " + response.statusCode());

            if (response.statusCode() == 403) {
                System.out.println("   ⚠️  Authorization required (admin only)");
            } else {
                System.out.println("   Response: " + response.body().substring(0, Math.min(100, response.body().length())));
            }

        } catch (Exception e) {
            System.out.println("   Error with normal delete: " + e.getMessage());
        }

        // Test 2: Type confusion in DELETE ID
        testDeleteTypeConfusion();

        // Test 3: DELETE non-existent book
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/books/999999"))
                    .header("Authorization", "Bearer " + authToken)
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("\n   Test 3: Delete non-existent book (999999)");
            System.out.println("   Status: " + response.statusCode());
            System.out.println("   Response: " + response.body().substring(0, Math.min(100, response.body().length())));

        } catch (Exception e) {
            System.out.println("   Error deleting non-existent book: " + e.getMessage());
        }
    }

    /**
     * NEW: Test DELETE endpoint with type confusion
     */
    private static void testDeleteTypeConfusion() {
        System.out.println("\n   🔢 Testing DELETE Type Confusion...");

        List<String> invalidIds = Arrays.asList(
                "abc",
                "-1",
                "0",
                "999999999999999999999999999999",
                "1.5",
                "1; DROP TABLE books; --",
                "<script>alert(1)</script>",
                "null",
                "true",
                "' OR '1'='1",
                "../etc/passwd",  // Path traversal attempt
                "1 OR 1=1"        // SQL injection pattern
        );

        for (String id : invalidIds) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/books/" + id))
                        .header("Authorization", "Bearer " + authToken)
                        .DELETE()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("     ID: " + id);
                System.out.println("     Status: " + response.statusCode());

                // Check for error messages that reveal too much information
                String body = response.body();
                if (body.contains("Exception") || body.contains("SQL") ||
                        body.contains("syntax") || body.contains("NumberFormatException")) {
                    System.out.println("     ⚠️  INFORMATION DISCLOSURE: " +
                            body.substring(0, Math.min(80, body.length())).replace("\n", " "));
                }

                Thread.sleep(100);

            } catch (Exception e) {
                System.out.println("     Error with ID '" + id + "': " + e.getMessage());
            }
        }
    }

    /**
     * Helper method to get first book ID from the system
     */
    private static Long getFirstBookId() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/books"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String body = response.body();
                // Simple parsing to find first book ID
                if (body.contains("\"id\":")) {
                    int start = body.indexOf("\"id\":") + 5;
                    int end = body.indexOf(",", start);
                    if (end == -1) end = body.indexOf("}", start);
                    String idStr = body.substring(start, end).trim();
                    return Long.parseLong(idStr);
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting book ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper method to create a test book for DELETE testing
     */
    private static Long createTestBook() {
        try {
            // First login as admin to create a book
            String adminJson = "{\"username\":\"admin\",\"password\":\"password123\"}";
            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(adminJson))
                    .build();

            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

            if (loginResponse.statusCode() == 200) {
                String body = loginResponse.body();
                int start = body.indexOf("\"token\":\"") + 9;
                int end = body.indexOf("\"", start);
                String adminToken = body.substring(start, end);

                // Create a test book
                String bookJson = "{\"title\":\"DELETE TEST BOOK\",\"author\":\"Test Author\",\"isbn\":\"DELETE-TEST-123\"}";
                HttpRequest createRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/books"))
                        .header("Authorization", "Bearer " + adminToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(bookJson))
                        .build();

                HttpResponse<String> createResponse = client.send(createRequest, HttpResponse.BodyHandlers.ofString());

                if (createResponse.statusCode() == 201 || createResponse.statusCode() == 200) {
                    // Try to parse the book ID from response
                    String responseBody = createResponse.body();
                    if (responseBody.contains("\"id\":")) {
                        int idStart = responseBody.indexOf("\"id\":") + 5;
                        int idEnd = responseBody.indexOf(",", idStart);
                        if (idEnd == -1) idEnd = responseBody.indexOf("}", idStart);
                        String idStr = responseBody.substring(idStart, idEnd).trim();
                        return Long.parseLong(idStr);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error creating test book: " + e.getMessage());
        }
        return null;
    }

    /**
     * Helper method to build JSON from Map
     */
    private static String buildJson(Map<String, Object> data) {
        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            if (!first) {
                json.append(",");
            }
            first = false;

            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(escapeJson((String) value)).append("\"");
            } else if (value instanceof Boolean) {
                json.append(value);
            } else if (value instanceof Number) {
                json.append(value);
            } else {
                json.append("\"").append(escapeJson(value.toString())).append("\"");
            }
        }

        json.append("}");
        return json.toString();
    }

    /**
     * Helper method to escape JSON strings
     */
    private static String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Test SQL Injection vulnerabilities
     */
    private static void testSqlInjection() {
        System.out.println("\n💉 Testing SQL Injection...");

        List<String> sqlInjectionPayloads = Arrays.asList(
                "' OR '1'='1",
                "' OR '1'='1' --",
                "' OR '1'='1' /*",
                "' UNION SELECT null,username,password,null FROM users --",
                "'; DROP TABLE books; --",
                "' OR 'a'='a",
                "' OR 1=1 --",
                "admin' --",
                "1' OR '1'='1",
                "' OR EXISTS(SELECT * FROM users) AND '1'='1"
        );

        for (String payload : sqlInjectionPayloads) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + payload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Payload: " + payload);
                System.out.println("   Status: " + response.statusCode());
                System.out.println("   Response length: " + response.body().length());

                // Check for signs of SQL injection success
                if (response.body().contains("\"books\":[")) {
                    try {
                        // Count how many books were returned
                        String body = response.body();
                        int bookCount = 0;
                        int index = body.indexOf("\"books\":[");
                        if (index != -1) {
                            String booksSection = body.substring(index + 8);
                            bookCount = countOccurrences(booksSection, "\"id\":");
                        }

                        if (bookCount > 4) { // More than initial 4 books
                            System.out.println("   ⚠️  POSSIBLE SQL INJECTION! Returned " + bookCount + " books");
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }

                Thread.sleep(100); // Small delay between requests

            } catch (Exception e) {
                System.out.println("   Error with payload '" + payload + "': " + e.getMessage());
            }
        }
    }

    /**
     * Test Type Confusion vulnerabilities
     */
    private static void testTypeConfusion() {
        System.out.println("\n🔢 Testing Type Confusion...");

        List<String> typeConfusionPayloads = Arrays.asList(
                "abc",          // String instead of number
                "-1",           // Negative number
                "0",            // Zero
                "999999999999999999999999999999",  // Very large number
                "1.5",          // Float instead of integer
                "1; DROP TABLE books",  // SQL in numeric field
                "<script>alert(1)</script>",  // XSS in numeric field
                "null",         // null string
                "true",         // boolean
                "1' OR '1'='1"  // SQL injection in numeric field
        );

        for (String payload : typeConfusionPayloads) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/borrow/" + payload))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{}"))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Payload: " + payload);
                System.out.println("   Status: " + response.statusCode());

                // Check for error messages that reveal too much information
                String body = response.body();
                if (body.contains("Exception") || body.contains("SQL") ||
                        body.contains("syntax") || body.contains("NumberFormatException")) {
                    System.out.println("   ⚠️  INFORMATION DISCLOSURE: " +
                            body.substring(0, Math.min(100, body.length())));
                }

                Thread.sleep(100);

            } catch (Exception e) {
                System.out.println("   Error with payload '" + payload + "': " + e.getMessage());
            }
        }
    }

    /**
     * Test Path Traversal attempts
     */
    private static void testPathTraversal() {
        System.out.println("\n📁 Testing Path Traversal...");

        List<String> pathTraversalPayloads = Arrays.asList(
                "../etc/passwd",
                "..\\..\\windows\\system32\\config\\SAM",
                "../../../etc/passwd",
                "....//....//etc/passwd",
                "%2e%2e%2fetc%2fpasswd",
                "..%2f..%2f..%2fetc%2fpasswd",
                "..%252f..%252f..%252fetc%252fpasswd"
        );

        for (String payload : pathTraversalPayloads) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + payload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Payload: " + payload);
                System.out.println("   Status: " + response.statusCode());

                Thread.sleep(100);

            } catch (Exception e) {
                System.out.println("   Error with payload '" + payload + "': " + e.getMessage());
            }
        }
    }

    /**
     * Test Authorization Bypass attempts
     */
    private static void testAuthorizationBypass() {
        System.out.println("\n🔓 Testing Authorization Bypass...");

        // Try to access admin endpoints as regular user
        try {
            // First login as regular user
            String json = "{\"username\":\"alice\",\"password\":\"password123\"}";
            HttpRequest loginRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/login"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

            if (loginResponse.statusCode() == 200) {
                String body = loginResponse.body();
                int start = body.indexOf("\"token\":\"") + 9;
                int end = body.indexOf("\"", start);
                String userToken = body.substring(start, end);

                // Try to add a book (admin-only endpoint)
                String bookJson = "{\"title\":\"Hacked Book\",\"author\":\"Hacker\",\"isbn\":\"999\"}";
                HttpRequest adminRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/books"))
                        .header("Authorization", "Bearer " + userToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(bookJson))
                        .build();

                HttpResponse<String> adminResponse = client.send(adminRequest, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Testing admin endpoint with user token");
                System.out.println("   Status: " + adminResponse.statusCode());

                if (adminResponse.statusCode() == 403) {
                    System.out.println("   ✅ Authorization working correctly");
                } else if (adminResponse.statusCode() == 200) {
                    System.out.println("   ❌ AUTHORIZATION BYPASS SUCCESSFUL!");
                }
            }

        } catch (Exception e) {
            System.out.println("   Error testing authorization: " + e.getMessage());
        }
    }

    /**
     * Test Input Validation
     */
    private static void testInputValidation() {
        System.out.println("\n📝 Testing Input Validation...");

        List<Map<String, String>> testCases = new ArrayList<>();

        // Extreme values for book addition
        Map<String, String> case1 = new HashMap<>();
        case1.put("title", "A".repeat(1000));  // Very long title
        case1.put("author", "B".repeat(1000)); // Very long author
        case1.put("isbn", "123");
        testCases.add(case1);

        Map<String, String> case2 = new HashMap<>();
        case2.put("title", "<script>alert('XSS')</script>");  // XSS attempt
        case2.put("author", "Author'); DROP TABLE books; --");  // SQL in author
        case2.put("isbn", "123' OR '1'='1");
        testCases.add(case2);

        Map<String, String> case3 = new HashMap<>();
        case3.put("title", "");  // Empty title
        case3.put("author", ""); // Empty author
        case3.put("isbn", "");
        testCases.add(case3);

        for (Map<String, String> testCase : testCases) {
            try {
                String json = String.format("{\"title\":\"%s\",\"author\":\"%s\",\"isbn\":\"%s\"}",
                        testCase.get("title").replace("\"", "\\\""),
                        testCase.get("author").replace("\"", "\\\""),
                        testCase.get("isbn").replace("\"", "\\\""));

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/books"))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Test case: " + testCase);
                System.out.println("   Status: " + response.statusCode());
                System.out.println("   Response: " + response.body().substring(0, Math.min(100, response.body().length())));

                Thread.sleep(100);

            } catch (Exception e) {
                System.out.println("   Error with test case: " + e.getMessage());
            }
        }
    }

    /**
     * Test Extreme Inputs
     */
    private static void testExtremeInputs() {
        System.out.println("\n🌋 Testing Extreme Inputs...");

        // Test with very large JSON
        try {
            StringBuilder largeJson = new StringBuilder("{\"title\":\"");
            for (int i = 0; i < 10000; i++) {
                largeJson.append("A");
            }
            largeJson.append("\",\"author\":\"Author\",\"isbn\":\"123\"}");

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/books"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .header("Content-Length", String.valueOf(largeJson.length()))
                    .POST(HttpRequest.BodyPublishers.ofString(largeJson.toString()))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("   Large input (10KB)");
            System.out.println("   Status: " + response.statusCode());

        } catch (Exception e) {
            System.out.println("   Error with large input: " + e.getMessage());
        }

        // Test with special characters
        try {
            String specialJson = "{\"title\":\"\\\"';SELECT * FROM books;--\",\"author\":\"\\0\\n\\r\\t\\\\\",\"isbn\":\"💣🎯🚀\"}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/books"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(specialJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("   Special characters input");
            System.out.println("   Status: " + response.statusCode());

        } catch (Exception e) {
            System.out.println("   Error with special characters: " + e.getMessage());
        }
    }

    /**
     * Test Mass Assignment
     */
    private static void testMassAssignment() {
        System.out.println("\n🎯 Testing Mass Assignment...");

        // Try to set admin role during registration
        try {
            String json = "{\"username\":\"hacker\",\"password\":\"hacker123\",\"role\":\"ADMIN\",\"admin\":true,\"privileged\":true}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/auth/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("   Trying to set admin role during registration");
            System.out.println("   Status: " + response.statusCode());
            System.out.println("   Response: " + response.body());

            // Try to login with the new user
            if (response.body().contains("success")) {
                String loginJson = "{\"username\":\"hacker\",\"password\":\"hacker123\"}";
                HttpRequest loginRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(loginJson))
                        .build();

                HttpResponse<String> loginResponse = client.send(loginRequest, HttpResponse.BodyHandlers.ofString());

                if (loginResponse.statusCode() == 200) {
                    System.out.println("   ⚠️  New user created successfully");
                    // Check if they can access admin endpoints
                }
            }

        } catch (Exception e) {
            System.out.println("   Error testing mass assignment: " + e.getMessage());
        }
    }

    /**
     * Test Error Handling
     */
    private static void testErrorHandling() {
        System.out.println("\n⚠️  Testing Error Handling...");

        // Test malformed JSON
        try {
            String malformedJson = "{title: \"Book\", author: \"Author\"}";  // Missing quotes

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/books"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(malformedJson))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("   Malformed JSON");
            System.out.println("   Status: " + response.statusCode());
            System.out.println("   Response: " + response.body().substring(0, Math.min(200, response.body().length())));

            // Check if stack trace is exposed
            if (response.body().contains("at ") && response.body().contains("Exception")) {
                System.out.println("   ❌ STACK TRACE EXPOSED!");
            }

        } catch (Exception e) {
            System.out.println("   Error testing error handling: " + e.getMessage());
        }

        // Test invalid endpoint
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/nonexistent"))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("   Non-existent endpoint");
            System.out.println("   Status: " + response.statusCode());

        } catch (Exception e) {
            System.out.println("   Error testing non-existent endpoint: " + e.getMessage());
        }
    }

    /**
     * Helper method to count occurrences
     */
    private static int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
}