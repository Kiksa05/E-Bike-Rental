# EcoRide Rental System

EcoRide Rental System is a full-stack web application that allows customers to browse and reserve electric bikes while providing administrators with tools to manage reservations and the e-bike fleet.

## Features

### Customer
- Register a new account
- Log in securely
- Browse available e-bikes
- Reserve an e-bike
- View reservation status

### Administrator
- View all reservation requests
- Approve or reject reservations
- Manage available e-bikes

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

1. Register a customer account.
2. Log in.
3. Browse available e-bikes.
4. Select an e-bike and create a reservation.
5. The reservation is submitted with a **Pending** status.
6. An administrator approves or rejects the reservation.
7. Once approved, the reservation becomes active.

---

## Future Improvements

- JWT authentication
- Password encryption
- Email notifications
- User profile management
- Interactive station map
- Online payment integration
- Reservation history

---

## Author

**Kristijan Stefanoski**
