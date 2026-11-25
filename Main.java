import java.util.*;
import java.io.*;

public class Main {

    static Scanner sc = new Scanner(System.in);

    static ArrayList<MenuItem> menu = new ArrayList<>();
    static Queue<Order> orders = new LinkedList<>();
    static Stack<OrderItem> cart = new Stack<>();

    static int nextOrderId = 101;
    static final String FILE = "orders.txt";

    // Menu Item class
    static class MenuItem {
        String code, name;
        int price;

        MenuItem(String code, String name, int price) {
            this.code = code;
            this.name = name;
            this.price = price;
        }
    }

    // Order Item class
    static class OrderItem {
        String code;
        int quantity;

        OrderItem(String code, int quantity) {
            this.code = code;
            this.quantity = quantity;
        }
    }

    // Order class
    static class Order {
        String id, customer;
        ArrayList<OrderItem> items;

        Order(String id, String customer,
              ArrayList<OrderItem> items) {
            this.id = id;
            this.customer = customer;
            this.items = items;
        }
    }

    // Initialize food menu
    static void initializeMenu() {
        menu.add(new MenuItem("M101", "Veg Burger", 120));
        menu.add(new MenuItem("M102", "Paneer Wrap", 150));
        menu.add(new MenuItem("M103", "Cold Coffee", 90));
    }

    // Find menu item
    static MenuItem findItem(String code) {
        for (MenuItem item : menu) {
            if (item.code.equalsIgnoreCase(code)) {
                return item;
            }
        }
        return null;
    }

    // Calculate total price
    static int calculateTotal(ArrayList<OrderItem> items) {
        int total = 0;

        for (OrderItem oi : items) {
            MenuItem item = findItem(oi.code);
            if (item != null) {
                total += item.price * oi.quantity;
            }
        }

        return total;
    }

    // Display menu
    static void displayMenu() {
        System.out.println("\n------ FOOD MENU ------");

        for (MenuItem item : menu) {
            System.out.println(
                item.code + " | " + item.name +
                " | " + item.price
            );
        }
    }

    // Place a new order
    static void placeOrder() {
        System.out.print("Customer Name: ");
        String customer = sc.nextLine().trim();

        if (customer.isEmpty()) {
            System.out.println("Invalid customer name.");
            return;
        }

        cart.clear();

        System.out.println("\nEnter item code and quantity.");
        System.out.println("Type DONE to finish.");
        System.out.println("Type UNDO to remove last added item.");

        while (true) {
            System.out.print("\nItem Code: ");
            String code = sc.nextLine().trim();

            if (code.equalsIgnoreCase("DONE")) {
                break;
            }

            if (code.equalsIgnoreCase("UNDO")) {
                if (!cart.empty()) {
                    cart.pop();
                    System.out.println("Last item removed.");
                } else {
                    System.out.println("Cart is empty.");
                }
                continue;
            }

            MenuItem item = findItem(code);

            if (item == null) {
                System.out.println("Item " + code + " not available.");
                continue;
            }

            System.out.print("Quantity: ");
            String input = sc.nextLine().trim();

            int quantity;

            try {
                quantity = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid quantity.");
                continue;
            }

            if (quantity <= 0) {
                System.out.println("Quantity must be positive.");
                continue;
            }

            cart.push(new OrderItem(item.code, quantity));
            System.out.println("Item added: " + item.name);
        }

        if (cart.empty()) {
            System.out.println("No items added. Order cancelled.");
            return;
        }

        ArrayList<OrderItem> orderItems =
            new ArrayList<>(cart);

        String orderId = "O" + nextOrderId++;

        Order order = new Order(
            orderId, customer, orderItems
        );

        orders.offer(order);

        System.out.println("\nOrder " + order.id +
                           " placed. " + customer +
                           " | " + getOrderDetails(order) +
                           " | Total: " +
                           calculateTotal(order.items));

        saveOrders();
        cart.clear();
    }

    // Generate order details
    static String getOrderDetails(Order order) {
        StringBuilder details = new StringBuilder();

        for (int i = 0; i < order.items.size(); i++) {
            OrderItem oi = order.items.get(i);
            MenuItem item = findItem(oi.code);

            if (i > 0) {
                details.append(" + ");
            }

            details.append(item.name)
                   .append(" x")
                   .append(oi.quantity);
        }

        return details.toString();
    }

    // Process next pending order
    static void processOrder() {
        if (orders.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }

        Order order = orders.poll();

        System.out.println("Order " + order.id +
                           " prepared.");

        saveOrders();
    }

    // Display pending orders
    static void displayPendingOrders() {
        if (orders.isEmpty()) {
            System.out.println("No pending orders.");
            return;
        }

        System.out.println("\n--- PENDING ORDERS ---");

        int count = 1;

        for (Order order : orders) {
            System.out.println(
                count++ + ". " + order.id +
                " | " + order.customer +
                " | " + getOrderDetails(order) +
                " | Total: " +
                calculateTotal(order.items)
            );
        }
    }

    // Sort menu by price
    static void sortMenu() {
        menu.sort(
            Comparator.comparingInt(item -> item.price)
        );

        System.out.println("\nMenu sorted by price:");

        for (MenuItem item : menu) {
            System.out.println(
                item.code + " | " + item.price
            );
        }
    }

    // Save pending orders to file
    static void saveOrders() {
        try (PrintWriter writer =
                 new PrintWriter(new FileWriter(FILE))) {

            writer.println("NEXT|" + nextOrderId);

            for (Order order : orders) {
                StringBuilder data = new StringBuilder();

                for (int i = 0; i < order.items.size(); i++) {
                    OrderItem oi = order.items.get(i);

                    if (i > 0) {
                        data.append(",");
                    }

                    data.append(oi.code)
                        .append(":")
                        .append(oi.quantity);
                }

                writer.println(
                    order.id + "|" +
                    order.customer + "|" +
                    data
                );
            }

        } catch (IOException e) {
            System.out.println("Error saving orders.");
        }
    }

    // Load orders when program starts
    static void loadOrders() {
        File file = new File(FILE);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                 new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] parts = line.split("\\|", -1);

                if (parts[0].equals("NEXT")) {
                    nextOrderId = Integer.parseInt(parts[1]);
                    continue;
                }

                if (parts.length != 3) {
                    continue;
                }

                ArrayList<OrderItem> items =
                    new ArrayList<>();

                if (!parts[2].isEmpty()) {
                    String[] savedItems = parts[2].split(",");

                    for (String saved : savedItems) {
                        String[] values = saved.split(":");

                        if (values.length != 2) {
                            continue;
                        }

                        String code = values[0];
                        int quantity = Integer.parseInt(values[1]);

                        if (findItem(code) != null &&
                            quantity > 0) {
                            items.add(
                                new OrderItem(code, quantity)
                            );
                        }
                    }
                }

                if (!items.isEmpty()) {
                    orders.offer(
                        new Order(parts[0], parts[1], items)
                    );
                }
            }

        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading saved orders.");
        }
    }

    // Main method
    public static void main(String[] args) {

        initializeMenu();
        loadOrders();

        System.out.println(
            "===== ONLINE FOOD ORDERING SYSTEM ====="
        );

        while (true) {

            System.out.println("\n1. View Menu");
            System.out.println("2. Place Order");
            System.out.println("3. Process Next Order");
            System.out.println("4. View Pending Orders");
            System.out.println("5. Sort Menu by Price");
            System.out.println("6. Exit");

            System.out.print("\nEnter your choice: ");

            String input = sc.nextLine().trim();

            int choice;

            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                continue;
            }

            switch (choice) {

                case 1:
                    displayMenu();
                    break;

                case 2:
                    placeOrder();
                    break;

                case 3:
                    processOrder();
                    break;

                case 4:
                    displayPendingOrders();
                    break;

                case 5:
                    sortMenu();
                    break;

                case 6:
                    saveOrders();
                    System.out.println("Thank you! Exiting...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}