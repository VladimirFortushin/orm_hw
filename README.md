# orm_hw (ORM subject final project of learning management system)

Учебная платформа для онлайн-курсов на **Spring Boot 4 + Hibernate 7 + PostgreSQL 18**.
Финальный проект по курсу «ORM и Hibernate»: 15 JPA-сущностей, все типы связей
(1-1, 1-M, M-M), ленивая загрузка, REST API, интеграционные тесты на Testcontainers.

---

## Стек

- **Java 17**
- **Spring Boot 4.1.1** (Spring Data JPA, Spring Web, Validation)
- **Hibernate ORM 7**
- **PostgreSQL 18** (в Docker для dev; Testcontainers для тестов)
- **Lombok**
- **springdoc-openapi 3.2.0** - Swagger UI
- **Testcontainers 2.x**

---

## Модель данных

15 сущностей покрывают предметную область учебной платформы:

| Сущность | Назначение | Ключевые связи |
|---|---|---|
| `User` | Пользователь (студент / преподаватель / админ) | 1-1 `Profile`, 1-M `Course` (teaches), 1-M `Enrollment`, `Submission`, `QuizSubmission`, `CourseReview` |
| `Profile` | Доп. информация о пользователе | 1-1 `User` |
| `Category` | Категория курсов | 1-M `Course` |
| `Course` | Учебный курс | M-1 `Category`, `User` (teacher); 1-M `CourseModule`, `Enrollment`, `CourseReview`; M-M `Tag` |
| `CourseModule` | Модуль курса (тема) | M-1 `Course`; 1-M `Lesson`; 1-1 `Quiz` |
| `Lesson` | Урок | M-1 `CourseModule`; 1-M `Assignment` |
| `Assignment` | Задание | M-1 `Lesson`; 1-M `Submission` |
| `Submission` | Решение задания студентом | M-1 `Assignment`, `User` (student) |
| `Quiz` | Тест к модулю | 1-1 `CourseModule`; 1-M `Question`, `QuizSubmission` |
| `Question` | Вопрос теста | M-1 `Quiz`; 1-M `AnswerOption` |
| `AnswerOption` | Вариант ответа | M-1 `Question` |
| `QuizSubmission` | Результат прохождения теста | M-1 `Quiz`, `User` (student) |
| `Enrollment` | Запись студента на курс (явная M-M) | M-1 `User`, `Course` |
| `CourseReview` | Отзыв о курсе | M-1 `Course`, `User` (student) |
| `Tag` | Тег курса | M-M `Course` (через `course_tag`) |

**Все коллекции - `FetchType.LAZY`**, включая `@ManyToOne`/`@OneToOne` (явно указано).
`spring.jpa.open-in-view=false` - сессия закрывается на выходе из сервиса, поэтому
lazy-проблемы ловятся сразу, а не «маскируются» open-in-view.

---

### Запуск

### Требования
- JDK 17+
- Maven 3.9+
- Docker Desktop (для PostgreSQL и тестов)

### 1. Поднять БД

```bash
docker compose up -d