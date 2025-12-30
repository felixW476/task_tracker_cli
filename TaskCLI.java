import java.util.ArrayList;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TaskCLI {

    public static void main(String[] args) {
        String cmd = args[0];
        ArrayList<Task> tasks = loadTask();

        if (cmd == null || args == null || args.length == 0 || cmd.isBlank()) {
            System.out.println("Invalid command or argument.");
            return;
        }

        switch (cmd) {

            case "add":
                if (args.length < 2) {
                    System.out.println("Missing description");
                    return;
                }
                addTask(tasks, args);
                saveTasks(tasks);
                break;
            case "update":
                if (args[1].isBlank() || args[2].isBlank()) {
                    System.out.println("Missing description or Task ID");
                    return;
                }
                updateTask(tasks, args);
                saveTasks(tasks);
                break;
            case "delete":
                if (args[1].isBlank()) {
                    System.out.print("Missing Task ID");
                }
                deleteTask(tasks, args);
                saveTasks(tasks);
                break;
            case "mark-in-progress":
                markInProgress(tasks, args);
                saveTasks(tasks);
                break;
            case "mark-done":
                markDone(tasks, args);
                saveTasks(tasks);
                break;
            case "list":
                if (args.length == 1) {
                    System.out.println(listAll(tasks));
                } else {
                    switch (args[1]) {
                        case "todo":
                            System.out.println(listToDo(tasks));
                            break;
                        case "in-progress":
                            System.out.println(listInProgress(tasks));
                            break;
                        case "done":
                            System.out.println(listDone(tasks));
                            break;
                    }
                }
                break;
        }

    }

    // FILE PERSISTANCE
    // read file of tasks.json
    private static ArrayList<Task> loadTask() {
        ArrayList<Task> task = new ArrayList<>();
        File file = new File("tasks.json");

        if (!file.exists()) { // if file does not exist, return empty list
            return task;
        }

        try {
            String content = new String(Files.readAllBytes(file.toPath())).trim();
            if (content.isEmpty()) {
                return task;
            }

            content = content.substring(1, content.length() - 1);
            if (content.isBlank()) {
                return task;
            }

            String[] taskObjects = content.split("\\},\\s*\\{");

            for (String taskJson : taskObjects) {

                taskJson = taskJson.replace("{", "").replace("}", "");
                String[] fields = taskJson.split(",");

                int id = 0;
                String description = "";
                String status = "";
                String createdAt = "";
                String updatedAt = "";

                // JSON field switch
                for (String field : fields) {
                    String[] pair = field.split(":", 2);
                    String key = pair[0].replace("\"", "").trim();
                    String value = pair[1].replace("\"", "").trim();

                    switch (key) {
                        case "id":
                            id = Integer.parseInt(value);
                            break;
                        case "description":
                            description = value;
                            break;
                        case "status":
                            status = value.toLowerCase();
                            break;
                        case "createdAt":
                            createdAt = value;
                            break;
                        case "updatedAt":
                            updatedAt = value;
                            break;
                    }
                }

                task.add(new Task(id, description, status, createdAt, updatedAt));
            }
        } catch (IOException e) {
            System.out.println("Error reading tasks from file: " + e.getMessage());
        }
        return task;
    }

    // write tasks to file
    private static void saveTasks(ArrayList<Task> tasks) {
        try (FileWriter fw = new FileWriter("tasks.json")) {
            fw.write("[\n");
            for (int i = 0; i < tasks.size(); i++) {
                Task t = tasks.get(i);
                fw.write(String.format(
                        "  {\"id\":%d,\"description\":\"%s\",\"status\":\"%s\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
                        t.id, t.description, t.status, t.createdAt, t.updatedAt));
                if (i < tasks.size() - 1)
                    fw.write(",\n");
            }
            fw.write("\n]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Commands Methods
    private static void addTask(ArrayList<Task> tasks, String[] args) {
        String description = args[1];
        int id = getNextId(tasks);
        String status = "todo";
        String time = timeStamp();

        Task newTask = new Task(id, description, status, time, time); // create a element of task
        tasks.add(newTask);
    }

    private static void updateTask(ArrayList<Task> tasks, String[] args) {
        int id = Integer.parseInt(args[1]);
        for (Task task : tasks) {
            if (task.id == id) {
                task.description = args[2];
                task.updatedAt = timeStamp(); // register the time that you update task
            }
        }
    }

    private static void deleteTask(ArrayList<Task> tasks, String[] args) {
        int id = Integer.parseInt(args[1]);

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).id == id) {
                tasks.remove(i);
                return; // stop after deleting one
            }
        }
    }

    private static void markInProgress(ArrayList<Task> tasks, String[] args) {
        int id = Integer.parseInt(args[1]);

        for (Task task : tasks) {
            if (task.id == id) {
                task.status = "in-progress";
            }
        }
    }

    private static void markDone(ArrayList<Task> tasks, String[] args) {
        int id = Integer.parseInt(args[1]);
        for (Task task : tasks) {
            if (task.id == id) {
                task.status = "done";
            }
        }
    }

    public static String listAll(ArrayList<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        for (Task task : tasks) { // iterate through all tasks
            sb.append("ID: ").append(task.id)
                    .append(", Description: ").append(task.description)
                    .append(", Status: ").append(task.status)
                    .append(", Created At: ").append(task.createdAt)
                    .append(", Updated At: ").append(task.updatedAt)
                    .append("\n");
        }
        return sb.toString();
    }

    // filter tasks by status
    public static String listToDo(ArrayList<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        for (Task task : tasks) {
            if (task.status.equals("todo")) {
                sb.append("ID: ").append(task.id)
                        .append(", Description: ").append(task.description)
                        .append(", Status: ").append(task.status)
                        .append(", Created At: ").append(task.createdAt)
                        .append(", Updated At: ").append(task.updatedAt)
                        .append("\n");
            }
        }
        return sb.toString();
    }

    public static String listInProgress(ArrayList<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        for (Task task : tasks) {
            if (task.status.equals("in-progress")) {
                sb.append("ID: ").append(task.id)
                        .append(", Description: ").append(task.description)
                        .append(", Status: ").append(task.status)
                        .append(", Created At: ").append(task.createdAt)
                        .append(", Updated At: ").append(task.updatedAt)
                        .append("\n");
            }
        }
        return sb.toString();
    }

    public static String listDone(ArrayList<Task> tasks) {
        StringBuilder sb = new StringBuilder();
        for (Task task : tasks) {
            if (task.status.equals("done")) {
                sb.append("ID: ").append(task.id)
                        .append(", Description: ").append(task.description)
                        .append(", Status: ").append(task.status)
                        .append(", Created At: ").append(task.createdAt)
                        .append(", Updated At: ").append(task.updatedAt)
                        .append("\n");
            }
        }
        return sb.toString();
    }

    public static int getNextId(ArrayList<Task> tasks) { // register the next ID as follows
        int maxId = 0;
        for (Task task : tasks) {
            if (task.getId() > maxId) {
                maxId = task.getId();
            }
        }
        return maxId + 1; // always start at base 1

    }

    public static String timeStamp() { // get the current time
        return LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

}

class Task {
    int id;
    String description;
    String status; // Todo, In-Progress, Done
    String createdAt;
    String updatedAt;

    // Constructor
    public Task(int id, String description, String status, String createdAt, String updatedAt) {
        this.id = id;
        this.description = description;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public String getStatus() {
        return this.status;
    }

    public String getCreatedAt() {
        return this.createdAt;
    }

    public String getUpdatedAt() {
        return this.updatedAt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ID: " + id + ", Description: " + description + ", Status: " + status +
                ", Created At: " + createdAt + ", Updated At: " + updatedAt;
    }
}