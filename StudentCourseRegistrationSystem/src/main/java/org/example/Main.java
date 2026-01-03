package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// 1. GENERICS (The Data Manager)
class Database<T> {
    private List<T> items = new ArrayList<>();

    public void add(T item) {
        items.add(item);
    }

    public List<T> getAll() {
        return items;
    }
}

// 2. DATA MODELS
class Course {
    private String courseId;
    private String courseName;
    private int credits;

    public Course(String courseId, String courseName, int credits) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.credits = credits;
    }

    public String getCourseId() { return courseId; }
    public String getCourseName() { return courseName; }

    @Override
    public String toString() {
        return courseId + ": " + courseName + " (" + credits + " Credits)";
    }
}

// 3. USER HIERARCHY
abstract class User {
    private String id;
    private String name;
    private String password;

    public User(String id, String name, String password) {
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public String getId() { return id; }
    public String getPassword() { return password; }
    public String getName() { return name; }

    public abstract void showDashboard(Scanner scanner, Database<Course> courseDB, Database<Student> studentDB, Database<Teacher> teacherDB);
}

// Child 1: Student
class Student extends User {
    private List<Course> myCourses = new ArrayList<>();
    private Map<String, String> grades = new HashMap<>();

    public Student(String id, String name, String password) {
        super(id, name, password);
    }

    public List<Course> getMyCourses() { return myCourses; }
    public void setGrade(String courseId, String grade) { grades.put(courseId, grade); }

    public void registerCourse(Course c) {
        if (!myCourses.contains(c)) {
            myCourses.add(c);
            System.out.println("Success: Enrolled in " + c.getCourseName());
        } else {
            System.out.println("Error: Already enrolled.");
        }
    }

    @Override
    public void showDashboard(Scanner scanner, Database<Course> courseDB, Database<Student> studentDB, Database<Teacher> teacherDB) {
        while (true) {
            System.out.println("\n--- STUDENT DASHBOARD (" + getName() + ") ---");
            System.out.println("1. View Available Courses");
            System.out.println("2. Register for Course");
            System.out.println("3. View My Grades");
            System.out.println("4. Logout");
            System.out.print("Choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                System.out.println("\nAvailable Courses:");
                for (Course c : courseDB.getAll()) {
                    System.out.println(c);
                }
            } else if (choice == 2) {
                System.out.print("Enter Course ID to Register: ");
                String cid = scanner.next();
                boolean found = false;
                for (Course c : courseDB.getAll()) {
                    if (c.getCourseId().equals(cid)) {
                        registerCourse(c);
                        found = true;
                        break;
                    }
                }
                if (!found) System.out.println("Course not found.");
            } else if (choice == 3) {
                System.out.println("\nMy Grades:");
                if (grades.isEmpty()) System.out.println("No grades assigned yet.");
                for (String cid : grades.keySet()) {
                    System.out.println("Course: " + cid + " | Grade: " + grades.get(cid));
                }
            } else if (choice == 4) {
                break;
            }
        }
    }
}

// Child 2: Teacher
class Teacher extends User {
    // NEW: List to track which courses this teacher teaches
    private List<Course> taughtCourses = new ArrayList<>();

    public Teacher(String id, String name, String password) {
        super(id, name, password);
    }

    public void assignCourse(Course c) {
        taughtCourses.add(c);
    }

    @Override
    public void showDashboard(Scanner scanner, Database<Course> courseDB, Database<Student> studentDB, Database<Teacher> teacherDB) {
        while (true) {
            System.out.println("\n--- TEACHER DASHBOARD (" + getName() + ") ---");
            System.out.println("1. View My Students");
            System.out.println("2. Assign Grade");
            System.out.println("3. Logout");
            System.out.print("Choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                if(taughtCourses.isEmpty()) {
                    System.out.println("You are not assigned to any courses.");
                } else {
                    for(Course c : taughtCourses) {
                        System.out.println("\nClass: " + c.getCourseName() + " (" + c.getCourseId() + ")");
                        System.out.println("  Students Enrolled:");
                        boolean found = false;
                        for (Student s : studentDB.getAll()) {
                            if(s.getMyCourses().contains(c)) {
                                System.out.println("  - " + s.getName() + " (ID: " + s.getId() + ")");
                                found = true;
                            }
                        }
                        if(!found) System.out.println("    (No students enrolled yet)");
                    }
                }

            } else if (choice == 2) {
                System.out.print("Enter Student ID: ");
                String sid = scanner.next();
                System.out.print("Enter Course ID: ");
                String cid = scanner.next();

                // Security check: Teacher can only grade their own course
                boolean isMyCourse = false;
                for(Course c : taughtCourses) {
                    if(c.getCourseId().equals(cid)) isMyCourse = true;
                }

                if(isMyCourse) {
                    System.out.print("Enter Grade (A/B/C/F): ");
                    String grade = scanner.next();
                    boolean found = false;
                    for (Student s : studentDB.getAll()) {
                        if (s.getId().equals(sid)) {
                            s.setGrade(cid, grade);
                            System.out.println("Grade assigned successfully.");
                            found = true;
                            break;
                        }
                    }
                    if (!found) System.out.println("Student not found.");
                } else {
                    System.out.println("Error: You do not teach this course.");
                }

            } else if (choice == 3) {
                break;
            }
        }
    }
}

// Child 3: Admin
class Admin extends User {

    public Admin(String id, String name, String password) {
        super(id, name, password);
    }

    @Override
    public void showDashboard(Scanner scanner, Database<Course> courseDB, Database<Student> studentDB, Database<Teacher> teacherDB) {
        while (true) {
            System.out.println("\n--- ADMIN DASHBOARD ---");
            System.out.println("1. Add New Course");
            System.out.println("2. Add New Student");
            System.out.println("3. Add New Teacher");
            System.out.println("4. Assign Course to Teacher"); // New Feature
            System.out.println("5. View All Users");
            System.out.println("6. Logout");
            System.out.print("Choice: ");
            int choice = scanner.nextInt();

            if (choice == 1) {
                System.out.print("Course ID: ");
                String id = scanner.next();
                System.out.print("Course Name: ");
                String name = scanner.next();
                System.out.print("Credits: ");
                int cr = scanner.nextInt();
                courseDB.add(new Course(id, name, cr));
                System.out.println("Course Added.");

            } else if (choice == 2) {
                System.out.print("Student ID: ");
                String id = scanner.next();
                System.out.print("Name: ");
                String name = scanner.next();
                System.out.print("Password: ");
                String pass = scanner.next();

                // VALIDATION CHECK
                if(pass.equals("nil") || pass.isEmpty()) {
                    System.out.println("Error: Password cannot be 'nil' or empty. Student NOT created.");
                } else {
                    studentDB.add(new Student(id, name, pass));
                    System.out.println("Student Added Successfully.");
                }

            } else if (choice == 3) {
                System.out.print("Teacher ID: ");
                String id = scanner.next();
                System.out.print("Name: ");
                String name = scanner.next();
                System.out.print("Password: ");
                String pass = scanner.next();

                // VALIDATION CHECK
                if(pass.equals("nil") || pass.isEmpty()) {
                    System.out.println("Error: Password cannot be 'nil' or empty. Teacher NOT created.");
                } else {
                    teacherDB.add(new Teacher(id, name, pass));
                    System.out.println("Teacher Added Successfully.");
                }

            } else if (choice == 4) {
                // Assign Course to Teacher Logic
                System.out.print("Enter Teacher ID: ");
                String tid = scanner.next();
                System.out.print("Enter Course ID: ");
                String cid = scanner.next();

                Teacher tFound = null;
                for(Teacher t : teacherDB.getAll()) if(t.getId().equals(tid)) tFound = t;

                Course cFound = null;
                for(Course c : courseDB.getAll()) if(c.getCourseId().equals(cid)) cFound = c;

                if(tFound != null && cFound != null) {
                    tFound.assignCourse(cFound);
                    System.out.println("Success: Assigned " + cFound.getCourseName() + " to " + tFound.getName());
                } else {
                    System.out.println("Error: Teacher or Course not found.");
                }

            } else if (choice == 5) {
                System.out.println("--- All Students ---");
                for(Student s : studentDB.getAll()) System.out.println(s.getId() + ": " + s.getName());
                System.out.println("--- All Teachers ---");
                for(Teacher t : teacherDB.getAll()) System.out.println(t.getId() + ": " + t.getName());
            } else if (choice == 6) {
                break;
            }
        }
    }
}

// 4. MAIN CONTROLLER
public class Main {

    static Database<Course> courseDB = new Database<>();
    static Database<Student> studentDB = new Database<>();
    static Database<Teacher> teacherDB = new Database<>();
    static List<Admin> admins = new ArrayList<>();

    public static void main(String[] args) {
        setupSystem();
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\nSTUDENT COURSE REGISTRATION SYSTEM");
            System.out.println("Designed by Isha Sadiq, Muhammad Talha, Muhammad Owais");
            System.out.println("Type 'exit' as ID to stop system.");
            System.out.print("Enter ID: ");
            String id = scanner.next();

            if(id.equalsIgnoreCase("exit")) break;

            System.out.print("Enter Password: ");
            String pass = scanner.next();

            User loggedInUser = login(id, pass);

            if (loggedInUser != null) {
                System.out.println("Login Successful! Welcome, " + loggedInUser.getName());
                loggedInUser.showDashboard(scanner, courseDB, studentDB, teacherDB);
            } else {
                System.out.println("Invalid Credentials.");
            }
        }
        scanner.close();
    }

    public static User login(String id, String pass) {
        for (Admin a : admins) if (a.getId().equals(id) && a.getPassword().equals(pass)) return a;
        for (Teacher t : teacherDB.getAll()) if (t.getId().equals(id) && t.getPassword().equals(pass)) return t;
        for (Student s : studentDB.getAll()) if (s.getId().equals(id) && s.getPassword().equals(pass)) return s;
        return null;
    }

    public static void setupSystem() {
        // 1. Admin
        admins.add(new Admin("admin", "SuperAdmin", "admin123"));

        // 2. Courses
        Course c1 = new Course("CSC101", "Object_Oriented_Programming", 3);
        Course c2 = new Course("CSC102", "Data_Structures", 3);
        Course c3 = new Course("CSC103", "Multicariable_Calculus", 3);
        Course c4 = new Course("CSC104", "OOP_Lab", 1);
        Course c5 = new Course("CSC105", "Linear_Algebra", 3);

        courseDB.add(c1); courseDB.add(c2); courseDB.add(c3);
        courseDB.add(c4); courseDB.add(c5);

        // 3. Teachers
        Teacher t1 = new Teacher("t01", "Sir. Sandesh Kumar", "sandesh123");
        Teacher t2 = new Teacher("t02", "Sir. Zafarullah", "zafar123");

        // IMPORTANT: Assigning courses so they can see students immediately
        t1.assignCourse(c1); // Sandesh Kumar teaches OOP
        t1.assignCourse(c4); // Sandesh Kumar teaches OOP Lab
        t2.assignCourse(c3); // Zafarullah teaches Calculus

        teacherDB.add(t1);
        teacherDB.add(t2);

        // 4. Students
        Student s1 = new Student("s01", "Isha", "isha123");
        Student s2 = new Student("s02", "Owais", "owais123");
        Student s3 = new Student("s03", "Talha", "talha123");

        // Register students in courses so Teachers have data to see
        s1.registerCourse(c1); // Isha takes OOP
        s2.registerCourse(c1); // Owais takes OOP
        s3.registerCourse(c3); // Talha takes Calculus

        studentDB.add(s1); studentDB.add(s2); studentDB.add(s3);
    }
}