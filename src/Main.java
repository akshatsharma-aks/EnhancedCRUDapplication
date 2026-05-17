/**
 * Main.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Entry point for the Persistent Task Management System.
 *
 * Responsibilities (thin controller — all logic lives in TaskManager):
 *   - Print the welcome banner
 *   - Drive the main menu loop
 *   - Delegate every menu action to the appropriate TaskManager method
 *   - Handle the Exit option cleanly
 *
 * Architecture Overview:
 *   Main.java  ──delegates──▶  TaskManager.java  ──uses──▶  Task.java
 *   (UI/menu)                  (CRUD + File I/O)            (Data model)
 *
 * Author  : [Your Name]
 * Project : Persistent Task Management System
 * Version : 1.0
 * ─────────────────────────────────────────────────────────────────────────────
 */

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        // Initialise the service layer.
        // The constructor automatically loads tasks from tasks.txt
        // so data from previous sessions is available immediately.
        TaskManager manager = new TaskManager();

        printWelcomeBanner();

        // ── Main application loop ──
        boolean running = true;
        while (running) {

            printMenu();

            // Read menu selection — handles non-integer input gracefully
            int choice = getMenuChoice(scanner);

            switch (choice) {
                case 1: manager.addTask(scanner);    break;
                case 2: manager.viewAllTasks();      break;
                case 3: manager.updateTask(scanner); break;
                case 4: manager.deleteTask(scanner); break;
                case 5:
                    // Tasks are saved after every operation, so no extra save needed here
                    running = false;
                    printExitMessage();
                    break;
                default:
                    System.out.println("  ✖  Invalid choice. Please select a number from 1 to 5.");
                    System.out.println();
            }
        }

        scanner.close();
    }

    // ─────────────────────────────────────────────
    //  HELPER — Safe Menu Choice Reader
    // ─────────────────────────────────────────────

    /**
     * Reads the user's menu selection without crashing on non-numeric input.
     * Returns -1 for invalid input, which triggers the switch default case.
     *
     * @param scanner Scanner to read from
     * @return        Integer choice entered by the user (or -1 for bad input)
     */
    private static int getMenuChoice(Scanner scanner) {
        System.out.print("  Enter your choice: ");
        String input = scanner.nextLine().trim();
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return -1; // handled by the switch default
        }
    }

    // ─────────────────────────────────────────────
    //  UI — Welcome Banner
    // ─────────────────────────────────────────────

    private static void printWelcomeBanner() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║     PERSISTENT TASK MANAGEMENT SYSTEM  v1.0          ║");
        System.out.println("  ║     Internship Project  ·  Java File I/O  ·  OOP     ║");
        System.out.println("  ╠══════════════════════════════════════════════════════╣");
        System.out.println("  ║  Tasks are auto-saved to tasks.txt after each        ║");
        System.out.println("  ║  operation and reloaded every time you start.        ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }

    // ─────────────────────────────────────────────
    //  UI — Main Menu
    // ─────────────────────────────────────────────

    private static void printMenu() {
        System.out.println("  ┌─────────────────────────────────────┐");
        System.out.println("  │              MAIN MENU              │");
        System.out.println("  ├─────────────────────────────────────┤");
        System.out.println("  │   1.  Add New Task                  │");
        System.out.println("  │   2.  View All Tasks                │");
        System.out.println("  │   3.  Update Task                   │");
        System.out.println("  │   4.  Delete Task                   │");
        System.out.println("  │   5.  Exit                          │");
        System.out.println("  └─────────────────────────────────────┘");
    }

    // ─────────────────────────────────────────────
    //  UI — Exit Message
    // ─────────────────────────────────────────────

    private static void printExitMessage() {
        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║   All tasks saved. See you next time!  Goodbye 👋    ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println();
    }
}
