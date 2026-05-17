/**
 * TaskManager.java
 * ─────────────────────────────────────────────────────────────────────────────
 * Service layer for the Persistent Task Management System.
 *
 * Responsibilities:
 *   - In-memory CRUD operations on the ArrayList<Task> store
 *   - File persistence: loading tasks from tasks.txt on startup,
 *     and saving the full list back after every change
 *   - Auto-incrementing task ID management
 *   - All display / formatting for the console output
 *
 * File Format (tasks.txt):
 *   Each line holds one task in pipe-delimited format:
 *   taskId|taskTitle|taskDescription|taskStatus
 *
 * Author  : [Your Name]
 * Project : Persistent Task Management System
 * Version : 1.0
 * ─────────────────────────────────────────────────────────────────────────────
 */

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class TaskManager {

    // ─────────────────────────────────────────────
    //  Constants
    // ─────────────────────────────────────────────

    /** Path to the persistence file — sits in the project's working directory */
    private static final String FILE_PATH = "tasks.txt";

    // ─────────────────────────────────────────────
    //  State
    // ─────────────────────────────────────────────

    /** In-memory list of all tasks — this is the single source of truth at runtime */
    private ArrayList<Task> taskList = new ArrayList<>();

    /**
     * Counter used to generate unique IDs.
     * Initialised to (max existing ID + 1) after loading from file,
     * so IDs never repeat across sessions.
     */
    private int idCounter = 1;

    // ─────────────────────────────────────────────
    //  Constructor — load file on startup
    // ─────────────────────────────────────────────

    /**
     * Initialises the TaskManager and immediately loads any previously
     * saved tasks from tasks.txt so the session picks up where it left off.
     */
    public TaskManager() {
        loadTasksFromFile();
    }

    // ═════════════════════════════════════════════
    //  1. ADD TASK
    // ═════════════════════════════════════════════

    /**
     * Prompts the user for a title and description, creates a new Task
     * with auto-generated ID and default status "Pending", adds it to
     * the list, and immediately persists the change to disk.
     *
     * @param scanner Scanner for reading console input
     */
    public void addTask(Scanner scanner) {
        printSectionHeader("ADD NEW TASK");

        System.out.print("  Enter Task Title       : ");
        String title = scanner.nextLine().trim();

        System.out.print("  Enter Task Description : ");
        String description = scanner.nextLine().trim();

        // Validate — both fields are mandatory
        if (title.isEmpty() || description.isEmpty()) {
            printError("Title and Description cannot be empty. Task not added.");
            return;
        }

        // Create and store the new task
        Task task = new Task(idCounter++, title, description, "Pending");
        taskList.add(task);

        // Persist immediately so the file is always in sync
        saveTasksToFile();
        printSuccess("Task added! ID: " + task.getTaskId() + " | Saved to " + FILE_PATH);
    }

    // ═════════════════════════════════════════════
    //  2. VIEW ALL TASKS
    // ═════════════════════════════════════════════

    /**
     * Renders all tasks in a clean tabular format.
     * If no tasks exist, a friendly message is shown instead.
     */
    public void viewAllTasks() {
        printSectionHeader("ALL TASKS");

        if (taskList.isEmpty()) {
            System.out.println("  No tasks found. Add a task from the main menu.");
            System.out.println();
            return;
        }

        // ── Table header ──
        System.out.printf("  %-5s  %-22s  %-32s  %-15s%n",
                          "ID", "TITLE", "DESCRIPTION", "STATUS");
        System.out.println("  " + "─".repeat(80));

        // ── One row per task ──
        for (Task t : taskList) {
            System.out.printf("  %-5d  %-22s  %-32s  %s%n",
                              t.getTaskId(),
                              truncate(t.getTaskTitle(),       22),
                              truncate(t.getTaskDescription(), 32),
                              formatStatus(t.getTaskStatus()));
        }

        System.out.println("  " + "─".repeat(80));
        System.out.printf("  Total: %d task(s)  |  File: %s%n%n", taskList.size(), FILE_PATH);
    }

    // ═════════════════════════════════════════════
    //  3. UPDATE TASK
    // ═════════════════════════════════════════════

    /**
     * Lets the user modify the title, description, or status of any existing task.
     * Pressing Enter on a field skips it (keeps the current value).
     * Saves to file after a successful update.
     *
     * @param scanner Scanner for reading console input
     */
    public void updateTask(Scanner scanner) {
        printSectionHeader("UPDATE TASK");

        if (taskList.isEmpty()) {
            System.out.println("  No tasks available to update.");
            System.out.println();
            return;
        }

        viewAllTasks();

        int id = getIntInput(scanner, "  Enter Task ID to update: ");
        Task task = findById(id);

        if (task == null) {
            printError("No task found with ID " + id + ".");
            return;
        }

        // Show existing values before editing
        System.out.println();
        System.out.println("  Current values:");
        System.out.println("    Title       : " + task.getTaskTitle());
        System.out.println("    Description : " + task.getTaskDescription());
        System.out.println("    Status      : " + task.getTaskStatus());
        System.out.println();
        System.out.println("  (Press Enter to keep the current value for any field)");
        System.out.println();

        // ── Title ──
        System.out.print("  New Title       : ");
        String newTitle = scanner.nextLine().trim();

        // ── Description ──
        System.out.print("  New Description : ");
        String newDesc = scanner.nextLine().trim();

        // ── Status ──
        System.out.println("  Status options  : 1. Pending   2. In Progress   3. Completed");
        int statusChoice = getIntInput(scanner, "  Select status (0 = keep current): ");

        // Apply only what the user changed
        if (!newTitle.isEmpty())  task.setTaskTitle(newTitle);
        if (!newDesc.isEmpty())   task.setTaskDescription(newDesc);

        switch (statusChoice) {
            case 1: task.setTaskStatus("Pending");      break;
            case 2: task.setTaskStatus("In Progress");  break;
            case 3: task.setTaskStatus("Completed");    break;
            case 0: /* keep existing */                 break;
            default:
                printError("Invalid status choice — status not changed.");
        }

        saveTasksToFile();
        printSuccess("Task ID " + id + " updated and saved to " + FILE_PATH);
    }

    // ═════════════════════════════════════════════
    //  4. DELETE TASK
    // ═════════════════════════════════════════════

    /**
     * Removes a task by ID after user confirmation, then saves the updated
     * list to disk so the deletion is permanent.
     *
     * @param scanner Scanner for reading console input
     */
    public void deleteTask(Scanner scanner) {
        printSectionHeader("DELETE TASK");

        if (taskList.isEmpty()) {
            System.out.println("  No tasks available to delete.");
            System.out.println();
            return;
        }

        viewAllTasks();

        int id = getIntInput(scanner, "  Enter Task ID to delete: ");
        Task task = findById(id);

        if (task == null) {
            printError("No task found with ID " + id + ".");
            return;
        }

        // Confirm before permanent deletion
        System.out.println("  Task: \"" + task.getTaskTitle() + "\"");
        System.out.print("  Confirm deletion? (yes / no): ");
        String confirm = scanner.nextLine().trim().toLowerCase();

        if (confirm.equals("yes") || confirm.equals("y")) {
            taskList.remove(task);
            saveTasksToFile();
            printSuccess("Task ID " + id + " deleted and file updated.");
        } else {
            System.out.println("  Deletion cancelled.");
            System.out.println();
        }
    }

    // ═════════════════════════════════════════════
    //  FILE I/O — SAVE
    // ═════════════════════════════════════════════

    /**
     * Writes the entire taskList to tasks.txt, overwriting any previous content.
     * Called automatically after every Add / Update / Delete operation.
     *
     * Why overwrite instead of append?
     *   Overwriting guarantees the file always exactly mirrors taskList.
     *   Appending would leave deleted/updated tasks in the file.
     *
     * Uses BufferedWriter for efficient, buffered writes.
     * Catches IOException to prevent the app from crashing on I/O errors.
     */
    public void saveTasksToFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {

            for (Task t : taskList) {
                // Each task occupies exactly one line in the file
                writer.write(t.toFileString());
                writer.newLine();
            }

            // BufferedWriter auto-flushes and closes via try-with-resources

        } catch (IOException e) {
            System.out.println("  [ERROR] Could not save to " + FILE_PATH + ": " + e.getMessage());
        }
    }

    // ═════════════════════════════════════════════
    //  FILE I/O — LOAD
    // ═════════════════════════════════════════════

    /**
     * Reads tasks.txt on startup and populates taskList.
     *
     * Behaviour:
     *   - If tasks.txt does not exist: starts with an empty list (first-run scenario).
     *   - If a line is malformed: it is silently skipped (no crash, no corruption).
     *   - After loading, idCounter is set to maxId + 1 so new IDs never collide
     *     with IDs that already exist in the file.
     *
     * Uses BufferedReader for efficient line-by-line reading.
     * Handles FileNotFoundException and IOException separately for clarity.
     */
    public void loadTasksFromFile() {
        File file = new File(FILE_PATH);

        // First run — file doesn't exist yet; that's perfectly fine
        if (!file.exists()) {
            System.out.println("  [INFO] No existing task file found. Starting fresh.");
            return;
        }

        int loadedCount = 0;
        int maxId       = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {

            String line;
            while ((line = reader.readLine()) != null) {

                // Delegate deserialisation to Task — returns null for bad lines
                Task task = Task.fromFileString(line);

                if (task != null) {
                    taskList.add(task);
                    loadedCount++;

                    // Track the highest ID seen so we can set idCounter correctly
                    if (task.getTaskId() > maxId) {
                        maxId = task.getTaskId();
                    }
                }
                // Silently skip null results (malformed lines)
            }

        } catch (FileNotFoundException e) {
            // Handled above via file.exists() — this branch is a safety net
            System.out.println("  [INFO] Task file not found. Starting with empty list.");
        } catch (IOException e) {
            System.out.println("  [ERROR] Failed to read " + FILE_PATH + ": " + e.getMessage());
        }

        // Set counter so next new task gets an ID higher than all loaded IDs
        idCounter = maxId + 1;

        if (loadedCount > 0) {
            System.out.println("  [INFO] Loaded " + loadedCount + " task(s) from " + FILE_PATH);
        }
    }

    // ═════════════════════════════════════════════
    //  HELPER — Find Task by ID
    // ═════════════════════════════════════════════

    /**
     * Linear search through taskList for a task matching the given ID.
     *
     * @param id The task ID to search for
     * @return   Matching Task object, or null if not found
     */
    private Task findById(int id) {
        for (Task t : taskList) {
            if (t.getTaskId() == id) return t;
        }
        return null;
    }

    // ═════════════════════════════════════════════
    //  HELPER — Safe Integer Input
    // ═════════════════════════════════════════════

    /**
     * Reads an integer from the user, re-prompting on invalid input.
     * Prevents NumberFormatException from crashing the application.
     *
     * @param scanner Scanner to read from
     * @param prompt  Prompt string displayed to the user
     * @return        A valid integer
     */
    private int getIntInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String raw = scanner.nextLine().trim();
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                printError("Invalid input. Please enter a whole number.");
            }
        }
    }

    // ═════════════════════════════════════════════
    //  HELPER — String Truncation
    // ═════════════════════════════════════════════

    /**
     * Truncates a string to fit within a column width.
     * Appends "..." when the string is longer than the allowed width.
     *
     * @param text  Original string
     * @param width Maximum column width
     * @return      Possibly truncated string
     */
    private String truncate(String text, int width) {
        if (text.length() <= width) return text;
        return text.substring(0, width - 3) + "...";
    }

    // ═════════════════════════════════════════════
    //  HELPER — Status Formatter
    // ═════════════════════════════════════════════

    /**
     * Prefixes a status string with a visual indicator for quick scanning.
     *
     * @param status Raw status string
     * @return       Status with an icon prefix
     */
    private String formatStatus(String status) {
        switch (status) {
            case "Pending":     return "[ ] Pending";
            case "In Progress": return "[~] In Progress";
            case "Completed":   return "[✓] Completed";
            default:            return status;
        }
    }

    // ═════════════════════════════════════════════
    //  UI HELPERS — Console Formatting
    // ═════════════════════════════════════════════

    private void printSectionHeader(String title) {
        System.out.println();
        System.out.println("  ════════════════════════════════════════════════");
        System.out.println("   " + title);
        System.out.println("  ════════════════════════════════════════════════");
    }

    private void printSuccess(String message) {
        System.out.println("  ✔  " + message);
        System.out.println();
    }

    private void printError(String message) {
        System.out.println("  ✖  ERROR: " + message);
        System.out.println();
    }
}
