# QuizMaster — JSP/Servlet Web Application

A simple application for creating, managing and playing quizzes. Developed using Java Servlet/JSP technology with the MVC pattern, Hibernate (JPA) ORM, and a MySQL database. Application consists of two parts: an administrative section, which allows authenticated users to create and manage quizzes, and a public section, which allows participants to play the quizzes.

The application provides:

- Registration and login for admin users

- Creation, editing, viewing, and deletion of quizzes (CRUD functionalities)

- Adding questions and answers to a quiz, with control of the minimum number of questions

- Viewing quiz details (title, image, questions, correct and incorrect answers)

- Super admin management of users (view, edit, and delete admin profiles)

- Authentication and protection of all /admin/* routes using a filter

## Technologies

- **Java 21 / 23**

- **JSP, Servlets**

- **JPA (Hibernate) + MySQL**

- **Gradle 8.5 + Gretty (Jetty)**

- **Vanilla JS, HTML, CSS**

---

# Architecture and Technologies

Architecture: MVC (Servlets = Controller, JSP = View, JPA entities = Model)
Back-end: Java 23, Jakarta Servlets/JSP (JSTL), Hibernate/JPA, MySQL
Front-end: JSP + HTML5, CSS (custom), JavaScript (vanilla)
Build/Run: Gradle Wrapper + Gretty (Jetty)

Authentication is based on HttpSession, and all admin routes are protected by a filter and available under /admin/*.


# Running (Gradle Wrapper)

The project uses Gradle Wrapper, so manual Gradle installation is not required.
```bash
./gradlew clean build
./gradlew appRun
```

# Database

CREATE DATABASE quizdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE quizdb;

CREATE TABLE users (
         id BIGINT AUTO_INCREMENT PRIMARY KEY,
         name VARCHAR(255) NOT NULL,
         password VARCHAR(255) NOT NULL,
         admin BOOLEAN NOT NULL,
         superadmin BOOLEAN NOT NULL
     );
     
CREATE TABLE quizzes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    image_url VARCHAR(512),
    pin VARCHAR(10) NOT NULL UNIQUE,
    is_active BOOLEAN DEFAULT FALSE,
    created_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES users(id)
);


CREATE TABLE questions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quiz_id BIGINT NOT NULL,
    question_text TEXT NOT NULL,
    duration_seconds INT NOT NULL CHECK (duration_seconds <= 60),
    points INT NOT NULL,
    question_order INT,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);


CREATE TABLE answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    question_id BIGINT NOT NULL,
    answer_text TEXT NOT NULL,
    is_correct BOOLEAN NOT NULL,
    FOREIGN KEY (question_id) REFERENCES questions(id)
);


CREATE TABLE players (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    quiz_id BIGINT NOT NULL,
    total_score INT DEFAULT 0,
    FOREIGN KEY (quiz_id) REFERENCES quizzes(id)
);


CREATE TABLE player_answers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    player_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL,
    answer_id BIGINT,
    is_correct BOOLEAN,
    answered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (player_id) REFERENCES players(id),
    FOREIGN KEY (question_id) REFERENCES questions(id),
    FOREIGN KEY (answer_id) REFERENCES answers(id)
);


# Roles and Permissions

Super Admin 

- Can view, edit and start all quizzes

- Can view and edit admin profiles (change username/password)

- Can delete admin accounts (deletion is blocked if the admin owns quizzes unless those quizzes are removed first)

Admin (editor)

- Can view, edit and start only their own quizzes

- Has no access to user management

