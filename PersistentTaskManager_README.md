# 📋 Persistent Task Management System

> A menu-driven Java console application with full CRUD operations and **file-based persistence**.
> Tasks survive application restarts — saved automatically to `tasks.txt`.
> Built as part of a Software Development Internship.

---

## 🧾 Project Overview

This application extends a basic Task Manager with **Java File I/O**, making all task data permanent. Every Add, Update, and Delete operation is instantly written to disk. When the application starts, it reloads all previous tasks automatically — no manual save required.

---

## ✨ Features

| Feature | Detail |
|---|---|
| ➕ Add Task | Title + description → auto ID + "Pending" status |
| 📋 View Tasks | Tabular display with status icons |
| ✏️ Update Task | Change title, description, or status (Enter = keep current) |
| 🗑️ Delete Task | Confirmation prompt before permanent removal |
| 💾 Auto-Save | File written after every operation |
| 🔄 Auto-Load | Tasks restored from `tasks.txt` on every startup |
| 🛡️ Safe Input | No crashes on letters, symbols, or empty input |
| 🆔 Smart IDs | IDs never repeat across sessions |

---

## 🗂️ Project Structure

```
PersistentTaskManager/
│
├── src/
│   ├── Task.java           # Data model — fields, getters/setters, serialisation
│   ├── TaskManager.java    # Service layer — CRUD logic + File I/O
│   └── Main.java           # Entry point — menu loop, delegates to TaskManager
│
├── tasks.txt               # Auto-generated at runtime (do NOT edit manually)
└── README.md
```

**Three-layer architecture:**
```
Main.java  ──delegates──▶  TaskManager.java  ──uses──▶  Task.java
(UI/menu)                  (CRUD + File I/O)            (Data model)
```

---

## 🔧 How File Persistence Works

### Save (`saveTasksToFile`)
After every Add / Update / Delete, the entire `ArrayList<Task>` is written to `tasks.txt` using `BufferedWriter`. Each task occupies one pipe-delimited line:

```
1|Design Database Schema|Create ERD and define all tables|Completed
2|Build REST API|Implement CRUD endpoints using Spring Boot|In Progress
3|Write Unit Tests|Cover all service-layer methods with JUnit|Pending
```

### Load (`loadTasksFromFile`)
On startup, `BufferedReader` reads `tasks.txt` line by line. Each line is handed to `Task.fromFileString()`, which parses the pipe-delimited fields and reconstructs a `Task` object. Malformed lines are silently skipped — no data corruption, no crash.

### ID Continuity
After loading, `idCounter` is set to `(max loaded ID) + 1`. New tasks always get fresh IDs, even across sessions.

---

## ⚙️ Requirements

- Java JDK 8 or higher
- Any terminal or command prompt

---

## 🚀 How to Compile and Run

```bash
# Step 1: Go to the src directory
cd PersistentTaskManager/src

# Step 2: Compile all three files together
javac Task.java TaskManager.java Main.java

# Step 3: Run from the src directory
#         (tasks.txt will be created here on first Add)
java Main
```

> **Important:** Run `java Main` from inside the `src/` directory so `tasks.txt` is created there and found on the next run.

---

## 🖥️ Sample Console Output

### Startup (existing data)
```
  [INFO] Loaded 3 task(s) from tasks.txt

  ╔══════════════════════════════════════════════════════╗
  ║     PERSISTENT TASK MANAGEMENT SYSTEM  v1.0          ║
  ║     Internship Project  ·  Java File I/O  ·  OOP     ║
  ╠══════════════════════════════════════════════════════╣
  ║  Tasks are auto-saved to tasks.txt after each        ║
  ║  operation and reloaded every time you start.        ║
  ╚══════════════════════════════════════════════════════╝
```

### View All Tasks
```
  ════════════════════════════════════════════════
   ALL TASKS
  ════════════════════════════════════════════════
  ID     TITLE                   DESCRIPTION                       STATUS
  ────────────────────────────────────────────────────────────────────────────────
  1      Design Database Schema  Create ERD and define all tab...  [✓] Completed
  2      Build REST API          Implement CRUD endpoints using...  [~] In Progress
  3      Write Unit Tests        Cover all service-layer method...  [ ] Pending
  ────────────────────────────────────────────────────────────────────────────────
  Total: 3 task(s)  |  File: tasks.txt
```

### Add Task
```
  ════════════════════════════════════════════════
   ADD NEW TASK
  ════════════════════════════════════════════════
  Enter Task Title       : Deploy to AWS
  Enter Task Description : Set up EC2 and configure environment variables
  ✔  Task added! ID: 4 | Saved to tasks.txt
```

### Invalid Input Handling
```
  Enter your choice: abc
  ✖  Invalid choice. Please select a number from 1 to 5.

  Enter Task Title       :
  ✖  ERROR: Title and Description cannot be empty. Task not added.
```

---

## 🧠 Concepts Demonstrated

- **OOP** — Encapsulation (Task), Service layer (TaskManager), Controller (Main)
- **File I/O** — `BufferedWriter`, `BufferedReader`, `FileWriter`, `FileReader`
- **Exception Handling** — `FileNotFoundException`, `IOException`, `NumberFormatException`
- **try-with-resources** — Auto-closes file streams safely
- **ArrayList** — Dynamic in-memory task store
- **Serialisation** — `toFileString()` / `fromFileString()` for pipe-delimited format
- **String formatting** — `String.format()`, `printf()`, `split()`, `trim()`
- **Switch-Case + Loops** — Menu navigation and repeat-until-exit pattern

---

## 👤 Author

**[Your Full Name]**
B.Tech CSE — [Year]
[College Name]
GitHub: [@yourusername](https://github.com/yourusername)

---

## 📄 License

Open source — free to use for learning and internship portfolio.
