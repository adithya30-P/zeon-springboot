# Zeon Skills — Java Spring Boot Backend

This is the Java/Spring Boot rebuild of the original Zeon Skills Flask project.
**Frontend is unchanged** (same HTML structure, same CSS, same Bootstrap, same
images/JS) — only the backend was rewritten, from Python/Flask/SQLAlchemy/SQLite
to Java/Spring Boot/JPA/MySQL.

## Stack mapping

| Original (Flask)        | Rebuilt (Spring Boot)                    |
|--------------------------|-------------------------------------------|
| Flask routes              | Spring MVC `@Controller` classes          |
| Jinja2 templates           | Thymeleaf + Thymeleaf Layout Dialect (same markup) |
| SQLAlchemy models          | JPA `@Entity` classes                     |
| SQLite (`users.db`)        | MySQL                                     |
| Flask `session` dict       | `HttpSession` (same session keys: username, email, role, user_id) |
| `flash()` / `get_flashed_messages()` | `FlashMessage` helper + `flashMessages` model attribute |

## How to run

1. **Install prerequisites:** JDK 17+, Maven, MySQL running locally.
2. **Create the database:**
   ```sql
   CREATE DATABASE zeon_skills;
   ```
3. **Set your MySQL credentials** in `src/main/resources/application.properties`
   (`spring.datasource.username` / `spring.datasource.password` — currently `root`/`root`).
4. **Run it:**
   ```bash
   mvn spring-boot:run
   ```
   or open the folder in IntelliJ / VS Code / Antigravity and run `ZeonSkillsApplication`.
5. Visit `http://localhost:8080/` — this is the login page (same as the original `/`).
6. **Create your first admin account** by registering with an email ending in `@zeon.com`
   (this matches the original's rule: `role = 'admin' if email.endswith('@zeon.com') else 'user'`).

Tables are auto-created on first run (`spring.jpa.hibernate.ddl-auto=update`). There is no seed
data — the original project didn't seed any either.

## ⚠️ Could not compile/run this project here

This sandbox has **no internet access**, so Maven couldn't download Spring Boot/MySQL
dependencies to actually compile and test the app. Every file was written carefully and
cross-checked line-by-line against your Flask source (routes ↔ controllers, model fields
↔ entities, template variables ↔ model attributes — all verified to match), but you should
run `mvn clean install` (or open it in Antigravity/IntelliJ) as the real first step, and
expect to fix minor compile issues if any slipped through.

## Flagged discrepancies (carried over from your original code, not "fixed")

Per your rule — original code is the source of truth, nothing invented — these known
oddities in the Flask app were **preserved exactly**, not corrected:

1. **`admin_dashboard.html` stats never render.** The original `/admin_dashboard` route only
   ever passes `username` to the template, even though the template references
   `total_categories`, `total_skills`, `attempted_skills`, `total_users`, and `recent_skills`.
   Those always rendered blank in Flask (Jinja treats undefined as falsy) — same here. Say
   the word if you want me to wire up real counts.
2. **`admin_skillresults.html`'s "Skill" column always shows "Python".** The `UserScore`
   model has no `skill` relationship, so `us.skill` was always undefined → the Jinja
   fallback `"Python"` always won. Preserved as a literal "Python" in Thymeleaf too.
3. **Pass/fail thresholds are inconsistent in the original.** The actual quiz grading
   (`/test/<id>`) uses **≥50%** to pass. The admin results table (`admin_skillresults.html`)
   independently badges **≥40%** as "Pass". Both preserved exactly as-is.
4. **`/admin_update_question` has no admin-role check** in the original Flask code (every
   other `admin_*` route checks `session.get('role') != 'admin'`; this one doesn't). This is
   a real security gap in the source project. I did not add a check, to stay faithful — but
   flagging it because it's worth fixing (just ask, and I'll add the same guard the other
   routes use).
5. **Passwords are stored and compared in plain text**, matching the original `User` model
   exactly (no hashing). Same note as above — easy to switch to `BCryptPasswordEncoder`,
   not done here since it wasn't in the source.
6. **`home.html` has a broken CSS reference** (`assets/css/responsive.css`, a path that
   doesn't exist in `static/`) and several images are referenced with plain relative paths
   (`static/images/...`) rather than proper URLs. Both behave identically to the original —
   preserved rather than "fixed", since fixing would change visible behavior.
7. **Original DB was SQLite, not MySQL.** Since the goal was Java + Spring Boot + **MySQL**,
   `application.properties` targets MySQL. There's no data to migrate (no seed data existed),
   so this only matters if you had manually added skills/questions/users to the old
   `instance/users.db` that you want carried over — let me know if so and I'll write a
   converter.

## Project layout

```
src/main/java/com/zeonskills/
  entity/        User, Skill, Question, Answer, UserAnswer, UserScore
  repository/    Spring Data JPA repositories (one per entity)
  service/       AuthService, SkillService, QuizService — business logic
  controller/    AuthController, UserController, AdminController — routes
  config/        session keys, flash-message helper, Thymeleaf layout dialect,
                 global model attributes (session user → templates), 404 handler
src/main/resources/
  templates/     Thymeleaf versions of every original template (same markup)
  static/        your original CSS/JS/images, copied unchanged
  application.properties
```
