/*
 *
 * Created by: George Papasotiriou
 * Date: 12/11/2025
 *
 * Main entry point for Library Management System API with security testing capabilities
 */

package com.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MyACGLibrary {
    public static void main(String[] args) {
        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════╗\n" +
                "║                                                       ║\n" +
                "║       📚 LIBRARY MANAGEMENT API - STARTING 📚        ║\n" +
                "║                                                       ║\n" +
                "╚═══════════════════════════════════════════════════════╝\n");

        SpringApplication.run(MyACGLibrary.class, args);

        System.out.println("\n" +
                "✅ SUCCESS! Library API is now running!\n" +
                "==========================================");
        System.out.println("🌐 Application URL: http://localhost:8080");
        System.out.println("🗄️  H2 Database Console: http://localhost:8080/h2-console");
        System.out.println("   JDBC URL: jdbc:h2:file:./data/librarydb");
        System.out.println("   Username: sa");
        System.out.println("   Password: (leave empty)");
        System.out.println("   File: ./data/librarydb.mv.db");
        System.out.println("==========================================");

        System.out.println("\n🔐 AUTHENTICATION ENDPOINTS:");
        System.out.println("   POST /api/auth/login     - Get JWT token");
        System.out.println("   POST /api/auth/register  - Create new user");

        System.out.println("\n📋 CORRECT AUTHENTICATION COMMAND:");
        System.out.println("   curl -X POST http://localhost:8080/api/auth/login \\");
        System.out.println("        -H \"Content-Type: application/json\" \\");
        System.out.println("        -d '{\"username\":\"admin\",\"password\":\"password123\"}'");

        System.out.println("\n📚 BOOK MANAGEMENT ENDPOINTS:");
        System.out.println("   GET  /api/books          - View all books");
        System.out.println("   POST /api/books          - Add new book (Admin only)");
        System.out.println("   PUT  /api/books/{id}     - Update book (Admin only)");
        System.out.println("   DELETE /api/books/{id}   - Delete book (Admin only)");
        System.out.println("   GET  /api/search?q=query - Search books (⚠️ has SQLi)");

        System.out.println("\n👥 USER ENDPOINTS:");
        System.out.println("   POST /api/borrow/{id}    - Borrow a book");
        System.out.println("   POST /api/return/{id}    - Return a book");
        System.out.println("   GET  /api/user/books     - View your borrowed books");

        System.out.println("\n" +
                "🔧 TEST CREDENTIALS:\n" +
                "   👑 Admin: username='admin', password='password123'\n" +
                "   👩 User:  username='alice', password='password123'\n" +
                "   👨 User:  username='bob',   password='password123'\n" +
                "   👨 User:  username='charlie', password='password123'");

        System.out.println("\n" +
                "⚠️  SECURITY NOTES (For Testing):\n" +
                "   • The /api/search endpoint has SQL injection vulnerability\n" +
                "   • Database is now persistent (file-based H2)\n" +
                "   • Audit logging enabled for security events\n" +
                "   • This is intentional for security testing purposes");

        System.out.println("\n" +
                "🚀 EXAMPLE WITH TOKEN:");
        System.out.println("   1. First get token:");
        System.out.println("      curl -X POST http://localhost:8080/api/auth/login \\");
        System.out.println("           -H \"Content-Type: application/json\" \\");
        System.out.println("           -d '{\"username\":\"admin\",\"password\":\"password123\"}'");
        System.out.println("   ");
        System.out.println("   2. Then use token (replace YOUR_TOKEN_HERE):");
        System.out.println("      curl -X GET http://localhost:8080/api/books \\");
        System.out.println("           -H \"Authorization: Bearer YOUR_TOKEN_HERE\"");

        System.out.println("\n" +
                "🔍 ENHANCED SECURITY TESTING TOOL:");
        System.out.println("   After starting, run in another terminal:");
        System.out.println("   cd target/classes");
        System.out.println("   java com.library.SecurityTester");
        System.out.println("   ");
        System.out.println("   New Database Vulnerability Tests:");
        System.out.println("   • Schema extraction attacks");
        System.out.println("   • Data exfiltration attempts");
        System.out.println("   • Database manipulation testing");
        System.out.println("   • Stored procedure attacks");
        System.out.println("   • Blind SQL injection");
        System.out.println("   • Time-based SQL injection");

        System.out.println("\n" +
                "💾 DATA PERSISTENCE:");
        System.out.println("   • Database file: ./data/librarydb.mv.db");
        System.out.println("   • Data persists between application restarts ✓");
        System.out.println("   • Changes to books/users are saved permanently");
        System.out.println("   • Audit logs track all database changes");
        System.out.println("   • Check: http://localhost:8080/api/database-status");

        System.out.println("\n" +
                "🔗 DATABASE CONNECTION TEST:");
        System.out.println("   • GET http://localhost:8080/api/database-status");
        System.out.println("   • Shows live database statistics");
        System.out.println("   • Verifies database is properly connected");

        System.out.println("\n" +
                "╔═══════════════════════════════════════════════════════╗\n" +
                "║     🎯 Ready for Enhanced Security Testing! 🎯       ║\n" +
                "╚═══════════════════════════════════════════════════════╝\n");
    }
}
// FOR CMD
// Login (Get Token)
// curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"password123\"}"
// Testing Borrowed Books
// curl -X GET http://localhost:8080/api/user/books -H "Authorization: Bearer %TOKEN%"
// Testing Search Endpoint (SQL Injection Test)
// curl -X GET "http://localhost:8080/api/search?q=' OR '1'='1" -H "Authorization: Bearer %TOKEN%" (Remove " from url)
// Testing get all books (with token)
//curl -X GET http://localhost:8080/api/books -H "Authorization: Bearer %TOKEN%"
// borrow book
// curl -X POST http://localhost:8080/api/borrow/2 -H "Content-Type: application/json" -d "{\"username\":\"admin\",\"password\":\"password123\"}"
