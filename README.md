# OPD Management Mini-Module

Patients → Appointments → Consultations, with Spring Boot, MySQL and Angular.

| Part | Technology |
|------|------------|
| Backend | Java 17+ (21 works), Spring Boot 3.5.0, Spring Web, Spring Data JPA / Hibernate, Bean Validation, Maven |
| Database | MySQL 8 (database name: `opd_management`) |
| Frontend | Angular 19, TypeScript, HttpClient, Router (Node.js 20 LTS or 22 LTS) |
| Tests | JUnit 5 + MockMvc on an in-memory H2 database (your MySQL is never touched) |

URLs when running: **Frontend** http://localhost:4200 · **Backend API** http://localhost:8080/api

---

## 1. Prerequisites

Install these first:

- **JDK 17 or newer** (JDK 21 is fine)
- **Apache Maven 3.9+**: download from https://maven.apache.org/download.cgi, unzip, and add its `bin` folder to your `PATH`. (Alternative: open `backend` in IntelliJ IDEA and run `OpdApplication`; no separate Maven install needed.)
- **Node.js 20 LTS or 22 LTS** (includes npm). Angular 19 does not run on Node 16/17/19.
- **MySQL 8** (MySQL Installer for Windows is fine). Remember the `root` password you set.

Angular CLI does **not** need to be installed globally. `npm install` puts it inside `frontend/node_modules`.

## 2. Check your versions (PowerShell)

```powershell
java -version
mvn -version
node -v
npm -v
mysql --version
```

If a command is "not recognized", that tool is not installed or not on your `PATH`. Open a **new** PowerShell window after changing `PATH`.

## 3. Create the database

The backend creates the database and tables automatically on first start. To create it yourself (recommended, so you can see it exists):

```powershell
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS opd_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

SQL files are in `database/`:

| File | Purpose |
|------|---------|
| `01_create_database.sql` | Creates the database |
| `02_schema.sql` | **Optional.** The same tables Hibernate would create. Only run it if you want to create tables manually *before* the first backend start |
| `03_verify.sql` | Queries to show saved data after the demo |

To run a file from PowerShell: `mysql -u root -p -e "source database/03_verify.sql"`

## 4. Configure the database login

Defaults are in `backend/src/main/resources/application.properties` and can be overridden by environment variables:

| Variable | Default | Meaning |
|----------|---------|---------|
| `DB_HOST` | `localhost` | MySQL host |
| `DB_PORT` | `3306` | MySQL port |
| `DB_NAME` | `opd_management` | Database name |
| `DB_USERNAME` | `root` | MySQL user |
| `DB_PASSWORD` | *(empty)* | MySQL password |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:4200` | Frontend origin(s), comma-separated |
| `APP_DEMO_DATA_ENABLED` | `false` | `true` inserts 3 sample patients + 1 appointment when the patients table is empty |

## 5. Start the backend (terminal 1)

```powershell
cd opd-management\backend
$env:DB_USERNAME = "root"
$env:DB_PASSWORD = "your_mysql_password"
mvn spring-boot:run
```

Wait for `Started OpdApplication`. The backend is at http://localhost:8080. Quick check: open http://localhost:8080/api/patients in a browser. You should see `[]`.

## 6. Install frontend dependencies (terminal 2)

```powershell
cd opd-management\frontend
npm install
```

## 7. Start Angular (terminal 2)

```powershell
npm start
```

Open http://localhost:4200. The API URL is configured in one place: `frontend/src/environments/environment.ts`.

## 8. REST API

| Method | URL | Purpose | Success |
|--------|-----|---------|---------|
| POST | `/api/patients` | Register a patient | 201 |
| GET | `/api/patients` | List patients | 200 |
| GET | `/api/patients/search?query=` | Search by name or phone | 200 |
| GET | `/api/patients/{id}` | One patient | 200 |
| POST | `/api/appointments` | Book an appointment | 201 |
| GET | `/api/appointments/today` | Today's appointments (server local date) | 200 |
| GET | `/api/appointments` | All appointments | 200 |
| GET | `/api/appointments/{id}` | One appointment | 200 |
| POST | `/api/consultations` | Start a consultation (status IN_PROGRESS) | 201 |
| PUT | `/api/consultations/{id}` | Save a draft (vitals and notes) | 200 |
| PUT | `/api/consultations/{id}/complete` | Save + complete (consultation and appointment become COMPLETED) | 200 |
| GET | `/api/consultations/appointment/{appointmentId}` | Existing consultation for an appointment | 200, or 204 if none yet |
| GET | `/api/consultations/patient/{patientId}` | Completed consultations of a patient | 200 |

Added beyond the required list (all are used by the UI): `PUT /api/consultations/{id}` and `GET /api/consultations/appointment/{appointmentId}`.

Errors are always JSON: `{ "status": 400, "error": "Bad Request", "message": "...", "fieldErrors": { "age": "..." } }`.
Codes used: 400 invalid input or business rule, 404 record not found, 409 conflict (duplicate or already completed).

### Business rules

- Patient: name, gender, phone required; age 1–120.
- Appointment: patient must exist; doctor name and date/time required.
- Consultation: one per appointment (also enforced by a UNIQUE key in MySQL).
- **Completion rule:** at least 2 vitals must be filled. This module has exactly two vital fields (blood pressure, temperature), so **both** are needed. The rule lives in `ConsultationService.MIN_VITALS_FOR_COMPLETION`.
- Completing a consultation and setting the appointment to COMPLETED happen in **one transaction**. If anything fails, neither changes.
- A completed consultation is read-only. Completing it again returns 409.

### Time zone assumptions

- Dates are stored as `DATETIME(6)` with **no time zone conversion**.
- The browser sends the time exactly as picked (for example `2026-10-09T14:30`).
- "Today" = the **server's local date** (midnight to midnight in the server's zone).
- This works for a local demo, where browser and server are the same computer. For a real deployment, store UTC.
- Past times are accepted on purpose, so "book for right now" always works in the demo.

## 9. Test the complete workflow (acceptance test)

1. Start backend, then frontend. Open http://localhost:4200.
2. **Patients** → register `Rahul Patel`, Male, 30, `9876543210`. Click **Register patient**.
3. Confirm Rahul appears in the table. Type `rahul` (or `98765`) in the search box. The table filters.
4. Click **Book** on Rahul's row (or go to **Appointments**). Choose a doctor, keep the default date and time (now), click **Book appointment**.
5. Rahul appears under **Today's appointments** with status *Scheduled*.
6. Click **Start consultation**.
7. Enter blood pressure `120/80`, temperature `98.6`, and some notes.
8. Click **Complete consultation**. A green message appears and the status changes to *Completed*.
9. Go to **Appointments**: the status shows *Completed*.
10. On the Consultations page, scroll to **Completed consultations**. Rahul's visit is shown with vitals and notes. (Or click **History** on his Patients row.)
11. Stop the backend (Ctrl+C) and start it again. Refresh the browser: all data is still there.
12. Show the data in MySQL:

```powershell
mysql -u root -p -e "source database/03_verify.sql"
```

### Run the backend tests

```powershell
cd opd-management\backend
mvn test
```

## 10. Common errors and fixes

**`Communications link failure` / `Connection refused`**: MySQL is not running. Start the MySQL service (`services.msc` → MySQL80 → Start). Check `DB_HOST` and `DB_PORT`.

**`Access denied for user 'root'@'localhost'`**: wrong password. Set `$env:DB_PASSWORD` again in the same terminal you run `mvn` in. Environment variables only live in the window where you set them.

**`Unknown database 'opd_management'`**: run the command in section 3. (The default URL also has `createDatabaseIfNotExist=true`, which needs a user with CREATE permission.)

**`Port 8080 was already in use`**: find and stop the other program:
```powershell
netstat -ano | findstr :8080
Stop-Process -Id <PID> -Force
```
Or run on another port: `mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8081"` and change `apiBaseUrl` in `environment.ts` to `http://localhost:8081/api`.

**Port 4200 in use**: `npm start -- --port 4300`, then start the backend with `$env:CORS_ALLOWED_ORIGINS = "http://localhost:4300"`.

**`invalid target release: 17` or `release version 17 not supported`**: Maven is using an older Java. Check `mvn -version`; set `JAVA_HOME` to a JDK 17+ folder.

**`mvn` is not recognized**: Maven's `bin` folder is not on `PATH`. Add it, then open a new PowerShell window.

**`npm install` fails or `npm.ps1 cannot be loaded because running scripts is disabled`**: run `npm.cmd install` and `npm.cmd start` instead, or run `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`. For other install failures: check `node -v` is 20 or 22, delete `frontend\node_modules`, run `npm cache verify`, then `npm install` again.

**Browser shows "Cannot reach the server"**: the backend is not running, or the URL in `environment.ts` is wrong.

**CORS error in the browser console** (`blocked by CORS policy`): the frontend is not running on an allowed origin. Open the app via exactly `http://localhost:4200` (not `127.0.0.1`), or set `CORS_ALLOWED_ORIGINS` to the origin you use and restart the backend.

## 11. How a request flows (for the interview)

Every module follows: **Angular component → Angular service (HttpClient) → Controller → Service → Repository (JPA/Hibernate) → MySQL**, and back as JSON.

- **Patient registration.** The form posts JSON to `POST /api/patients`. `@Valid` checks the DTO. `PatientService` trims values and `PatientRepository.save` inserts a row. The saved patient comes back as `201` and the table reloads. Search calls `/search`, which uses a derived query on name (any case) or phone.
- **Appointment booking.** The dropdown is filled from `GET /api/patients`. `POST /api/appointments` sends `patientId`, doctor and date/time. `AppointmentService` loads the patient (404 if missing) and saves the appointment as SCHEDULED. `/today` filters `>= today 00:00 and < tomorrow 00:00`.
- **Consultation.** Opening a visit loads the appointment and any existing consultation. Save draft creates (`POST`) or updates (`PUT`) the consultation, and the appointment becomes IN_PROGRESS. Complete calls `PUT /{id}/complete`: in one `@Transactional` method it checks the two-vitals rule, sets the consultation and the appointment to COMPLETED, and commits both together. The history table calls `/patient/{id}`, which returns completed visits only, newest first.

## Project structure

```
opd-management/
├── README.md
├── .gitignore
├── database/                 SQL scripts (create database, optional schema, verify queries)
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/example/opd/
│       │   ├── OpdApplication.java
│       │   ├── config/        AppConfig (Clock), WebConfig (CORS), DemoDataLoader
│       │   ├── controller/    Patient / Appointment / Consultation REST controllers
│       │   ├── dto/           request and response records + ApiError
│       │   ├── entity/        Patient, Appointment, Consultation + enums
│       │   ├── exception/     custom exceptions + GlobalExceptionHandler
│       │   ├── repository/    Spring Data JPA repositories
│       │   └── service/       business logic and transactions
│       ├── main/resources/application.properties
│       └── test/              OpdApiIntegrationTest + application-test.properties (H2)
└── frontend/
    ├── package.json, angular.json, tsconfig*.json
    └── src/
        ├── environments/environment.ts   (API base URL)
        ├── styles.css
        └── app/
            ├── app.* (shell, routes, config)
            ├── core/ (http-error, date-utils)   models/   services/   shared/
            └── patients/   appointments/   consultation/
```
