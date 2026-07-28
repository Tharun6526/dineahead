# 🍽️ DineAhead - Smart Restaurant Pre-Ordering System

DineAhead is a full-stack restaurant pre-ordering platform that lets customers order food before they arrive at a restaurant. Customers pick an arrival time, restaurants prepare food on that schedule, and orders are securely verified using a Pickup Order Number and PIN.

The goal of DineAhead is to reduce customer waiting time and improve restaurant kitchen management.

---

## 🚀 Features

### 👤 Customer Features

- User registration and login
- JWT authentication
- Browse nearby restaurants
- View restaurant menus
- Add food items to cart
- Place advance orders
- Select arrival time
- Custom kitchen start time
- Online and cash payment options
- Receive pickup order number
- Receive secure 4-digit PIN
- Real-time order status tracking
- Delay arrival notification
- Cancel orders
- View order history

### 🏪 Restaurant Features

- Restaurant login
- Restaurant dashboard
- View customer orders
- Automatic kitchen scheduling
- Manage order lifecycle:

  ```
  PENDING → PREPARING → READY → SERVED
  ```

- Update order status
- View complete order details
- Verify customer orders securely
- Manage menu items (add, update, delete)

---

## 🔐 Security Features

- JWT based authentication
- Spring Security authorization
- Role-based access control
- Unique order PIN generation
- Pickup order number verification
- Prevention of unauthorized food collection
- Customer no-show tracking
- Cash payment disabled after repeated no-shows

---

## 🏗️ Architecture

```
                Customer
                   │
                   ▼
            React Frontend
                   │
                   ▼
         Spring Boot REST API
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
  PostgreSQL Database    JWT / Spring Security
        │
        ▼
  Restaurant Dashboard
```

Order status updates are pushed in real time via Spring WebSocket, so both customers and restaurant dashboards stay in sync without polling.

---

## 🛠️ Tech Stack

**Frontend**
- React.js
- JavaScript
- HTML / CSS
- Fetch API

**Backend**
- Java
- Spring Boot
- Spring MVC
- Spring Security
- Spring Data JPA
- Spring WebSocket
- Hibernate
- REST API
- Maven

**Database**
- PostgreSQL

**Tools**
- IntelliJ IDEA
- VS Code
- Postman
- Git / GitHub

---

## 📂 Project Structure

```
DineAhead
│
├── backend
│   ├── controller
│   ├── service
│   ├── repository
│   ├── model
│   ├── dto
│   ├── security
│   └── scheduler
│
└── frontend
    ├── components
    ├── pages
    ├── api
    └── services
```

---

## ⚙️ Backend Setup

**Clone the repository**

```bash
git clone https://github.com/Tharun6526/dineahead.git
```

**Navigate to the backend**

```bash
cd dineahead/backend
```

**Configure the database**

Update `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dineahead
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**Run the backend**

```bash
mvn spring-boot:run
```

Backend URL: `http://localhost:8080`

---

## ⚙️ Frontend Setup

**Navigate to the frontend**

```bash
cd frontend
```

**Install packages**

```bash
npm install
```

**Start the application**

```bash
npm start
```

Frontend URL: `http://localhost:3000`

---

## 🔄 Order Workflow

```
Customer places order
        │
        ▼
Order created (PENDING)
        │
        ▼
Kitchen scheduler starts
        │
        ▼
Restaurant prepares food
        │
        ▼
Order READY
        │
        ▼
Customer arrives
        │
        ▼
Waiter verifies pickup number + PIN
        │
        ▼
Order SERVED
```

---

## 🔑 Order Verification System

Every order contains:

- Order ID
- Pickup order number
- 4-digit PIN
- Customer information
- Restaurant information

Food cannot be collected using only a PIN. The waiter must verify:

1. Pickup order number
2. Customer PIN
3. Restaurant ID

This prevents unauthorized people from collecting another customer's order.

---

## 📌 API Endpoints

**Authentication**

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Log in |

**Orders**

| Method | Endpoint | Description |
|--------|----------|--------------|
| POST | `/api/orders` | Place an order |
| GET | `/api/orders/my` | Get the current customer's orders |
| GET | `/api/orders/restaurant/{restaurantId}` | Get orders for a restaurant |
| POST | `/api/orders/verify-pin` | Verify a customer's order |
| PATCH | `/api/orders/{id}/status` | Update order status |

---

## 🗄️ Database Entities

Main entities: `User`, `Restaurant`, `MenuItem`, `Order`, `OrderItem`, `Payment`, `Review`

```
User
 └── Orders

Restaurant
 ├── Menu Items
 └── Orders

Order
 └── Order Items
```

---

## 🚀 Future Enhancements

- Razorpay payment integration
- Google Maps restaurant distance
- AI-based preparation time prediction
- Mobile application
- Restaurant analytics
- Customer rewards system

---

## 👨‍💻 Developer

**Tharun Aluri**
Computer Science Engineering (AI & ML)
GitHub: [Tharun6526](https://github.com/Tharun6526)

---

## 📄 License

This project is created for educational and demonstration purposes.
