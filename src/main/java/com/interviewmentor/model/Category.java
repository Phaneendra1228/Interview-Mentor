package com.interviewmentor.model;

import java.awt.Color;

/**
 * Technical interview topic categories.
 */
public enum Category {
    DATA_STRUCTURES("Data Structures", "DS", new Color(0x7C3AED), "data_structures.json",
            "Arrays, Linked Lists, Stacks, Queues, Trees, Graphs, Hash Tables"),
    ALGORITHMS("Algorithms", "AL", new Color(0x2563EB), "algorithms.json",
            "Sorting, Searching, DP, Greedy, Recursion, Divide & Conquer"),
    OPERATING_SYSTEMS("Operating Systems", "OS", new Color(0x059669), "operating_systems.json",
            "Processes, Threads, Memory, Scheduling, Deadlocks, File Systems"),
    DBMS("Database Management", "DB", new Color(0xD97706), "dbms.json",
            "SQL, Normalization, Transactions, Indexing, ER Diagrams, ACID"),
    JAVA("Java Programming", "JV", new Color(0xDC2626), "java_programming.json",
            "OOP, Collections, Exceptions, Multithreading, Streams, JVM"),
    PYTHON("Python Programming", "PY", new Color(0x0891B2), "python_programming.json",
            "Data Types, Comprehensions, Decorators, Generators, OOP"),
    OOP("OOP Concepts", "OO", new Color(0x8B5CF6), "oop_concepts.json",
            "Inheritance, Polymorphism, Abstraction, Encapsulation, SOLID"),
    COMPUTER_NETWORKS("Computer Networks", "CN", new Color(0xF59E0B), "computer_networks.json",
            "OSI Model, TCP/IP, HTTP, DNS, Routing, Subnetting"),
    BEHAVIORAL("Behavioral / HR", "HR", new Color(0xEC4899), "behavioral.json",
            "STAR Method, Leadership, Teamwork, Conflict Resolution");

    private final String displayName;
    private final String shortCode;
    private final Color color;
    private final String jsonFile;
    private final String description;

    Category(String displayName, String shortCode, Color color, String jsonFile, String description) {
        this.displayName = displayName;
        this.shortCode = shortCode;
        this.color = color;
        this.jsonFile = jsonFile;
        this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public String getShortCode()   { return shortCode; }
    public Color getColor()        { return color; }
    public String getJsonFile()    { return jsonFile; }
    public String getDescription() { return description; }

    /**
     * Find Category by its name() string (e.g. "DATA_STRUCTURES").
     */
    public static Category fromString(String name) {
        try {
            return Category.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
