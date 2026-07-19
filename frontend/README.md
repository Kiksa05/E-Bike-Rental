# EcoRide Rentals - Frontend

A React-based frontend application for the EcoRide Rentals system. The application allows customers to browse available e-bikes, register an account, log in, and submit rental requests. Administrators can manage reservations through the backend.

---

## Technologies Used

- React
- React Router DOM
- Axios
- SweetAlert2
- CSS3

---

## Features

### Customer
- Register a new account
- Log in securely
- View available e-bikes
- Create rental reservations
- View rental pricing

### Administrator
- Access the admin dashboard
- Manage rental requests
- Approve or reject reservations
- Manage available e-bikes

---

## Prerequisites

Before running the project, make sure you have:

- Node.js (v18 or newer recommended)
- npm

---

## Installation

Clone the repository:

```bash
git clone <repository-url>
```

Navigate to the project:

```bash
cd ecoride-frontend
```

Install dependencies:

```bash
npm install
```

---

## Running the Application

Start the development server:

```bash
npm start
```

The application will be available at:

```
http://localhost:3000
```

The backend API should also be running (default):

```
http://localhost:8080
```

---

## Project Structure

```
src/
│
├── components/
├── services/
├── App.js
├── App.css
└── index.js
```

---

## Backend

This frontend communicates with the EcoRide Spring Boot REST API.

Make sure the backend is running before using the application.

---

## Future Improvements

- JWT authentication
- User profile management
- Station map integration
- Reservation history
- Responsive mobile layout

---

## Author

Kristijan Stefanoski
