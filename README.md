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

### Поднять БД

```bash
docker compose up -d
```

### Запустить приложение

```
mvn spring-boot:run
```
### Открыть Swagger UI

http://localhost:8080/swagger-ui.html
Все эндпоинты документированы, можно вызывать из браузера
---

## Параметры подключения

Параметры БД вынесены в `application.yml` и переопределяются через переменные окружения
(пароли в коде не хранятся):

| Переменная | Значение по умолчанию |
|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/orm` |
| `DB_USER` | `orm` |
| `DB_PASSWORD` | `orm` |

Схема БД создаётся автоматически (`spring.jpa.hibernate.ddl-auto: update`).
При первом запуске `DataInitializer` (профиль `!test`) наполняет БД демо-данными:
2 преподавателя, 2 студента, 2 курса с модулями и уроками, тест, отзыв, сдача задания.

---

## Тесты

```bash
mvn test
```

**8 интеграционных тестов** на реальном PostgreSQL 18 в Testcontainers
(singleton-контейнер — запускается один раз на весь прогон и переиспользуется
между тест-классами).

### Что проверяет каждый тест

| Тест | Что проверяет | Ожидаемый результат |
|---|---|---|
| `CascadeIntegrationTest.cascadePersist_savesModulesAndLessonsTogether` | `cascade = CascadeType.ALL` на `Course.modules` и `CourseModule.lessons`: сохранение одного Course `save()`-ом должно каскадно сохранить вложенные модули и уроки | После `em.clear()` повторная загрузка через `findByIdWithModulesAndLessons` возвращает курс с 1 модулем и 1 уроком, у урока корректный title |
| `CascadeIntegrationTest.cascadeDelete_deletesWholeSubtree` | Удаление `Course` каскадно (`cascade=ALL`, `orphanRemoval=true`) удаляет всё поддерево: модули → уроки → задания → сдачи | После `delete(course)` + `flush` таблицы `modules`, `lessons`, `assignments`, `submissions` пусты по этому поддереву |
| `CascadeIntegrationTest.removingFromCollection_orphanRemoval_deletesChild` | `orphanRemoval=true`: удаление ребёнка из коллекции `course.modules` удаляет запись из БД | После `course.removeModule(m2)` + `flush` вызов `moduleRepository.findById(m2Id)` возвращает `Optional.empty()` |
| `LazyLoadingTest.accessingLazyCollectionOutsideTransaction_throwsLazyInitializationException` | **Демонстрация lazy-проблемы** (требование ТЗ, этап 3): курс загружен в одной транзакции, возвращён наружу, обращение к `course.getModules().size()` **вне сессии** | Бросается `LazyInitializationException` (проверяется через `assertThrows`, без привязки к тексту — в Hibernate 7 формат сообщения отличается от Hibernate 5/6) |
| `LazyLoadingTest.joinFetchLoadsCollection_eagerly_avoidsException` | **Решение проблемы** через `JOIN FETCH`: `findByIdWithModules` инициализирует коллекцию внутри транзакции | После закрытия транзакции `loaded.getModules().size()` == 2 без исключения; элементы коллекции доступны |
| `LazyLoadingTest.accessingLazyCollectionInsideTransaction_works` | Открытая сессия → lazy подгружается прозрачно (используется `TransactionTemplate`) | Внутри `tx.execute(...)` вызов `course.getModules().stream().map(...)` работает, возвращает ожидаемые title'ы |
| `CourseWorkflowTest.fullCourseScenario_endToEnd` | **Сквозной сценарий** бизнес-логики: teacher + student → category → course → module → lesson → assignment → submission → grade → enrollment → quiz → quiz-submission → review. Плюс negative-кейсы | Все шаги отрабатывают; повторные `enroll` / `submit` / `review` того же студента бросают `BusinessException`; оценка вне `[0, maxScore]` — `BusinessException`; зачисление `TEACHER` в студенты — `BusinessException`; отзыв от незачисленного студента — `BusinessException`; правильный ответ на тест → score = 100, неправильный → 0 |
| `CourseWorkflowTest.courseUpdate_usesDirtyChecking` | Обновление managed-entity через **dirty checking** (без явного `save()`): `courseService.update(...)` меняет поля у загруженной сущности, изменения должны уйти в БД на коммите | Повторное чтение `courseService.getById(...)` возвращает обновлённые `title` и `description` |

### Как устроены тесты

- Все тесты наследуют `AbstractIntegrationTest` — базовый класс с singleton-контейнером
  `PostgreSQLContainer<>("postgres:18-alpine")`, который поднимается **один раз на JVM** в
  `static { }`. Это критично: Spring кэширует `ApplicationContext` (вместе с `HikariPool`) между
  тест-классами, и если контейнер останавливать на `@Container`, закэшированный пул будет
  стучаться на уже закрытый порт и падать с `Connection refused`.
- `@ActiveProfiles("test")` → подгружается `application-test.yml` с `ddl-auto: create-drop`.
  Каждый прогон начинается с чистой схемы.
- `@SpringBootTest` поднимает полный контекст (JPA, сервисы, репозитории), но **без** Tomcat —
  `webEnvironment` по умолчанию `MOCK`, что ускоряет прогон.
- `DataInitializer` под `@Profile("!test")` — демо-данные в тестах не создаются.
- Тесты на CRUD и каскады помечены `@Transactional` на уровне класса — каждый тест
  откатывается автоматически, БД остаётся чистой между тестами.
- `LazyLoadingTest` **намеренно** без `@Transactional`, иначе сессия живёт весь тест и
  `LazyInitializationException` не выбросится. Помечен `@DirtiesContext(AFTER_CLASS)`, чтобы
  данные не протекали в следующий класс.

---

## REST API

### Курсы

| Метод | Путь | Описание |
|---|---|---|
| GET | `/api/courses` | Список курсов (с кол-вом студентов и средним рейтингом) |
| GET | `/api/courses/{id}` | Полный курс с модулями, уроками, заданиями |
| POST | `/api/courses` | Создать курс (JSON-body `CreateCourseRequest`) |
| PUT | `/api/courses/{id}` | Обновить курс |
| DELETE | `/api/courses/{id}` | Удалить курс (каскадно) |
| POST | `/api/courses/{id}/enroll?studentId=` | Записать студента |
| GET | `/api/courses/{id}/enrollments` | Список записанных |
| POST | `/api/courses/{id}/modules?title=&orderIndex=` | Добавить модуль |
| POST | `/api/courses/modules/{moduleId}/lessons?title=` | Добавить урок |
| POST | `/api/courses/{id}/reviews?studentId=&rating=&comment=` | Оставить отзыв |
| GET | `/api/courses/{id}/reviews` | Список отзывов |

### Пользователи

| Метод | Путь | Описание |
|---|---|---|
| GET | `/api/users` | Все пользователи |
| GET | `/api/users/{id}` | Пользователь + профиль |
| POST | `/api/users?name=&email=&role=` | Создать |
| PUT | `/api/users/{id}/profile?bio=&avatarUrl=` | Обновить профиль |
| GET | `/api/users/{id}/enrollments` | Курсы пользователя |
| GET | `/api/users/{id}/submissions` | Сдачи пользователя |

### Задания и тесты

| Метод | Путь | Описание |
|---|---|---|
| POST | `/api/assignments/{id}/submit?studentId=&content=` | Сдать задание |
| PUT | `/api/submissions/{id}/grade?score=&feedback=` | Оценить |
| GET | `/api/assignments/{id}/submissions` | Все сдачи по заданию |
| GET | `/api/quizzes/{id}` | Тест с вопросами и вариантами |
| POST | `/api/quizzes/modules/{moduleId}?title=&timeLimit=` | Создать тест |
| POST | `/api/quizzes/{id}/submit?studentId=` | Пройти тест |

**Пример прохождения теста:**

```bash
curl -X POST 'http://localhost:8080/api/quizzes/1/submit?studentId=3' \
  -H 'Content-Type: application/json' \
  -d '{"answers": {"1": [1], "2": [4, 5]}}'
```

`answers` - `Map<questionId, List<optionId>>`. Для `SINGLE_CHOICE` в списке один ID,
для `MULTIPLE_CHOICE` — несколько. Балл = процент полностью верных ответов
(множество выбранных == множество правильных).

### Обработка ошибок

Все ошибки возвращаются как `ProblemDetail` (RFC 7807):

- `404 Not Found` — `NotFoundException` (например, курс с несуществующим id)
- `400 Bad Request` — `BusinessException` (нарушено бизнес-правило) /
  `MethodArgumentNotValidException` (ошибки валидации DTO)
- `500 Internal Server Error` — непредвиденные ошибки

