# EcoRide Rental System

EcoRide Rental System is a full-stack web application that allows customers to browse and reserve electric bikes while providing administrators with tools to manage reservations and the e-bike fleet.

## Features

### Customer
- Register a new account (passwords hashed with BCrypt, unique email enforced)
- Log in securely (JWT tokens, valid for 2 hours)
- Browse available e-bikes
- Reserve an e-bike on an hourly calendar: pick a day in the next two weeks and see which hours are free, taken or past, then click the hours you want (or a first and a last hour to fill in the ones between). The passport number is pre-filled from your profile
- **My reservations:** see all your reservations with their price and status (Pending, Approved, Rejected, Cancelled)
- Cancel your own reservation while it is pending, or approved but not started yet
- **Profile:** edit your name, email, phone and passport number
- Change your password (requires the current password, at least 8 characters)

### Administrator
- Approve or reject reservation requests
- **All reservations:** see every customer's reservations, newest first, filtered by status, and cancel any that have not started
- **Users:** edit any profile, set a new password, and switch users between the user and admin roles
- Disable or re-enable accounts
- Manage available e-bikes

### Booking rules
- Reservations start and end on the hour (13:00, 14:00, ...). The current hour can still be booked, but earlier ones cannot.
- A bike cannot be booked twice for overlapping times. A new reservation is refused if it overlaps a pending or approved reservation for that bike, while back-to-back bookings (10:00-12:00, then 12:00-14:00) are fine.
- Two bookings sent at the same moment are handled safely: the bike is locked while a reservation is checked and saved, so only one of them succeeds.
- A rejected or cancelled reservation frees its time slot again. Cancelled reservations stay in the history.
- Booking a bike blocks only the hours it is booked for. The same bike can be reserved for tomorrow or any other free time.
- A bike's Available / Not Available status is set by admins only, for example while it is being repaired. Reservations never change it.

### Accounts and data retention
- Accounts are **disabled, never deleted**, because customer data must be kept for at least two years.
- Disabling an account logs the user out on their next request and blocks new logins. Reservations that have not finished yet are removed, which frees those time slots, while past rentals stay as history.
- Every request checks the account behind the token, so disabling a user or changing a role takes effect immediately rather than when the token expires.
- Admins cannot change their own role or disable their own account, so the last admin cannot lock themselves out.

---

## Tech Stack

### Frontend
- React
- React Router DOM
- Axios
- SweetAlert2
- CSS3

### Backend
- Spring Boot
- Spring Security (OAuth2 resource server with JWT)
- Spring Data JPA
- RESTful API
- Maven

### Database
- MySQL

---

## Project Structure

```text
EcoRide-Rental-System/
│
├── frontend/          # React application
├── src/               # Spring Boot source code
├── pom.xml            # Maven configuration
├── README.md
└── ...
```

---

## Prerequisites

Before running the project, ensure you have installed:

- Java 17 or later
- Node.js (v18 or later recommended)
- npm
- Maven
- MySQL

---

## Run with Docker

The quickest way to start everything (requires Docker Desktop). First copy `.env.example` to `.env` and set `JWT_SECRET` to a long random value of at least 32 characters, then run:

```bash
docker compose up -d --build
```

| Service | Address |
|---|---|
| Frontend | http://localhost:3000 |
| Backend | http://localhost:8081 |
| MySQL | `127.0.0.1:3307` (user `root`, password `root`) |

Demo logins, both with the password `demo1234`: `kiksa@admin.com` (admin) and `user@demo.local` (user).

- `docker compose down` stops everything and keeps the data.
- `docker compose down -v` also deletes the data, so the next start reloads `database/demo.sql`.

Email sending is currently disabled. If you enable it, provide the Gmail app password through the `MAIL_PASSWORD` environment variable and never commit it.

---

## Getting Started

### 1. Clone the repository

```bash
git clone <repository-url>
```

---

### 2. Configure the database

Create a MySQL database named:

```text
ecoride-rental
```

Import `database/demo.sql` into it to get the demo accounts, bikes and stations. Skip this if the tables already exist.

Copy `.env.example` to `.env` in the project root and set `JWT_SECRET`. The backend will not start without it.

New accounts are always normal users. After that, admins change roles on the **Users** page. Only the very first admin of a fresh database has to be set directly:

```sql
UPDATE customer SET role = 'admin' WHERE email = 'someone@example.com';
```

Update the database credentials in:

```text
src/main/resources/application.properties
```

---

### 3. Run the Backend

#### Option 1 (Recommended)

Open the project in IntelliJ IDEA or Eclipse and run:

```text
EcoRideRentalApplication.java
```

located at:

```text
src/main/java/com/example/EcoRide/Rental/EcoRideRentalApplication.java
```

#### Option 2

Run using Maven from the project root:

```bash
mvn spring-boot:run
```

The backend will start on:

```text
http://localhost:8080
```

---

### 4. Run the Frontend

Navigate to the frontend project:

```bash
cd frontend
```

Install dependencies:

```bash
npm install
```

Start the application:

```bash
npm start
```

The frontend will be available at:

```text
http://localhost:3000
```

---

## Application Workflow

1. Register a customer account and log in.
2. Optionally add your passport number on the **Profile** page.
3. Browse available e-bikes, pick one, then click the free hours you want on the calendar. Hours already booked by someone else show as taken.
4. The reservation is submitted with a **Pending** status. If the bike is already booked for that time, you are asked to pick another time or bike.
5. An administrator approves or rejects it on the **To review** page.
6. Follow the status on **My reservations**. Once approved, the bike is yours for that time, and others can still book it for other times.

---

## Future Improvements

- Email notifications
- Interactive station map
- Online payment integration

---

## Author

**Kristijan Stefanoski**
