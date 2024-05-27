package com.example.rsubd;

import com.mongodb.MongoClientSettings;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class DryCleaningApp {
    private static final String DATABASE_NAME = "drycleaning";

    public static void main(String[] args) {
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.hosts(List.of(
                                new ServerAddress("localhost", 27017)
                        )))
                .build();

        try (MongoClient mongoClient = MongoClients.create(settings)) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("Choose a collection:");
                System.out.println("1. Customers");
                System.out.println("2. Orders");
                System.out.println("3. Invoices");
                System.out.println("4. Services");
                System.out.println("5. Employees");
                System.out.println("6. Items");
                System.out.println("7. Exit\n> ");

                int collectionChoice = scanner.nextInt();
                scanner.nextLine(); // Consume newline

                if (collectionChoice == 7) {
                    System.out.println("Exiting...");
                    break;
                }

                String collectionName = getCollectionName(collectionChoice);

                while (true) {
                    System.out.println("Choose an option:");
                    System.out.println("1. Display all documents");
                    System.out.println("2. Add a document");
                    System.out.println("3. Delete a document");
                    System.out.println("4. Find a document");
                    System.out.println("5. Sort documents");
                    System.out.println("6. Back to collections\n> ");

                    int choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline

                    if (choice == 6) {
                        break; // Go back to collections
                    }

                    switch (choice) {
                        case 1:
                            displayAllDocuments(database, collectionName);
                            break;
                        case 2:
                            addDocument(scanner, database, collectionName);
                            break;
                        case 3:
                            deleteDocument(scanner, database, collectionName);
                            break;
                        case 4:
                            findDocument(scanner, database, collectionName);
                            break;
                        case 5:
                            sortDocuments(scanner, database, collectionName);
                            break;
                        default:
                            System.out.println("Invalid option. Please try again.");
                    }
                }
            }
        }
    }

    private static String getCollectionName(int choice) {
        switch (choice) {
            case 1:
                return "Customers";
            case 2:
                return "Orders";
            case 3:
                return "Invoices";
            case 4:
                return "Services";
            case 5:
                return "Employees";
            case 6:
                return "Items";
            default:
                throw new IllegalArgumentException("Invalid collection choice.");
        }
    }

    private static void displayAllDocuments(MongoDatabase database, String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        for (Document doc : collection.find()) {
            System.out.println(doc.toJson());
        }
    }

    private static void addDocument(Scanner scanner, MongoDatabase database, String collectionName) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document document = new Document();

        // Add document specific fields based on collection
        switch (collectionName) {
            case "Customers":
                addCustomerDocument(scanner, document);
                break;
            case "Orders":
                addOrderDocument(scanner, database, document);
                break;
            case "Invoices":
                addInvoiceDocument(scanner, database, document);
                break;
            case "Services":
                addServiceDocument(scanner, document);
                break;
            case "Employees":
                addEmployeeDocument(scanner, document);
                break;
            case "Items":
                addItemDocument(scanner, document);
                break;
        }

        collection.insertOne(document);
        System.out.println("Document added successfully.");
    }

    private static void addCustomerDocument(Scanner scanner, Document document) {
        System.out.println("Enter customer name:");
        String name = scanner.nextLine();
        System.out.println("Enter customer email:");
        String email = scanner.nextLine();
        document.append("name", name)
                .append("email", email);
    }

    private static void addOrderDocument(Scanner scanner, MongoDatabase database, Document document) {
        System.out.println("Enter item name:");
        String name = scanner.nextLine();
        System.out.println("Enter customer ID:");
        String customerId = scanner.nextLine();
        System.out.println("Enter order date (YYYY-MM-DD):");
        String date = scanner.nextLine();
        document.append("name", name)
                .append("customer_id", customerId)
                .append("date", date);

        // Add items to the order
        List<Document> items = addItemsToOrder(scanner, database);
        document.append("items", items);
    }

    private static List<Document> addItemsToOrder(Scanner scanner, MongoDatabase database) {
        List<Document> items = new ArrayList<>();
        MongoCollection<Document> itemsCollection = database.getCollection("Items");

        while (true) {
            System.out.println("Add item to order (Y/N)?");
            String choice = scanner.nextLine();
            if (choice.equalsIgnoreCase("N")) {
                break;
            }
            System.out.println("Enter item name:");
            String itemName = scanner.nextLine();

            Document item = itemsCollection.find(Filters.eq("name", itemName)).first();
            if (item != null) {
                items.add(item);
            } else {
                System.out.println("Item not found. Please enter a valid item name.");
            }
        }
        return items;
    }

    private static void addInvoiceDocument(Scanner scanner, MongoDatabase database, Document document) {
        System.out.println("Enter order ID:");
        String orderId = scanner.nextLine();
        System.out.println("Enter item name:");
        String name = scanner.nextLine();
        System.out.println("Enter invoice date (YYYY-MM-DD):");
        String date = scanner.nextLine();
        document.append("order_id", orderId)
                .append("name", name)
                .append("date", date)
                .append("status", "pending");
    }

    private static void addServiceDocument(Scanner scanner, Document document) {
        System.out.println("Enter service name:");
        String name = scanner.nextLine();
        System.out.println("Enter service price:");
        double price = scanner.nextDouble();
        document.append("name", name)
                .append("price", price);
    }

    private static void addEmployeeDocument(Scanner scanner, Document document) {
        System.out.println("Enter employee name:");
        String name = scanner.nextLine();
        System.out.println("Enter employee position:");
        String position = scanner.nextLine();
        document.append("name", name)
                .append("position", position);
    }

    private static void addItemDocument(Scanner scanner, Document document) {
        System.out.println("Enter item name:");
        String name = scanner.nextLine();
        System.out.println("Enter item description:");
        String description = scanner.nextLine();
        System.out.println("Enter item price:");
        double price = scanner.nextDouble();
        scanner.nextLine(); // Consume newline
        document.append("name", name)
                .append("description", description)
                .append("price", price);
    }

    private static void deleteDocument(Scanner scanner, MongoDatabase database, String collectionName) {
        System.out.println("Enter document NAME to delete:");
        String documentName = scanner.nextLine();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        collection.deleteOne(Filters.eq("name", documentName));
        System.out.println("Document deleted successfully.");
    }

    private static void findDocument(Scanner scanner, MongoDatabase database, String collectionName) {
        System.out.println("Enter field name:");
        String field = scanner.nextLine();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document document = collection.find(Filters.eq("name", field)).first();
        if (document != null) {
            System.out.println(document.toJson());
        } else {
            System.out.println("Document not found.");
        }
    }

    private static void sortDocuments(Scanner scanner, MongoDatabase database, String collectionName) {
        System.out.println("Enter field to sort by:");
        String field = scanner.nextLine();
        MongoCollection<Document> collection = database.getCollection(collectionName);
        for (Document doc : collection.find().sort(Sorts.ascending(field))) {
            System.out.println(doc.toJson());
        }
    }
}

