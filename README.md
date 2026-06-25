# 💰 SmartExpense

> **Smart Expense Management & ITR Assistant for Indian Salaried Employees**

SmartExpense is a full-stack web application that simplifies expense tracking and Income Tax Return (ITR) preparation for Indian salaried employees. It enables users to securely manage expenses, upload receipts, automatically categorize them into Indian tax deduction sections, and generate ITR-ready reports.

---

## 📖 About the Project

Managing expenses throughout the financial year is difficult for many salaried employees. Receipts are often lost, expenses are forgotten, and organizing documents during tax filing becomes time-consuming.

SmartExpense addresses these challenges by providing a centralized platform that helps users:

* Track daily expenses
* Store digital receipts securely
* Automatically categorize expenses for Indian tax deductions
* View spending analytics
* Generate ITR-ready reports

---

## 🚀 Features

### 🔐 Authentication

* User Registration & Login
* JWT Authentication
* Secure Password Encryption
* Role-based authorization

### 💳 Expense Management

* Add Expenses
* Edit Expenses
* Delete Expenses
* Expense History
* Search & Filter Expenses

### 📷 Receipt Upload

* Upload Receipt Images
* OCR-based Text Extraction
* Automatic Amount & Date Detection
* Receipt Storage

### 📊 Dashboard

* Total Expenses
* Monthly Spending
* Category-wise Analytics
* Expense Trends

### 🇮🇳 Indian Tax Support

* Automatic Tax Category Mapping
* 80C
* 80D
* HRA
* Fuel
* Medical
* Other Tax Categories

### 📑 Reports

* Generate ITR-ready Excel Reports
* Financial Year Summary
* Detailed Transaction History

---

# 🏗️ System Architecture

```
React Frontend
        │
        ▼
Spring Boot REST API
        │
        ▼
Business Services
        │
        ▼
PostgreSQL Database

Cloudinary → Receipt Storage

OCR Engine → Receipt Processing
```

---

# 🛠️ Tech Stack

## Frontend

* React
* Vite
* Tailwind CSS
* Axios
* Chart.js

## Backend

* Java
* Spring Boot
* Spring Security
* Spring Data JPA
* JWT Authentication

## Database

* MySQL

## Storage

* Cloudinary

## OCR

* Tesseract OCR

## Deployment

* Vercel
* Render
* Supabase

---

# 📂 Project Structure

```
SmartExpense

backend/
frontend/
docs/
screenshots/
database/
README.md
```

---

# 🔄 Workflow

1. User registers and logs in.
2. Uploads a receipt image.
3. OCR extracts receipt information.
4. Expense is categorized automatically.
5. User verifies the extracted information.
6. Expense is saved.
7. Dashboard analytics are updated.
8. User generates an ITR-ready report whenever required.

---

# 📸 Screenshots

> Screenshots will be added after completing the UI.

* Login
* Dashboard
* Upload Receipt
* OCR Result
* Expense List
* Analytics
* ITR Report

---

# 📊 Key Highlights

* OCR-based Receipt Processing
* Automatic Expense Categorization
* Indian Tax Section Support
* JWT Authentication
* RESTful APIs
* Responsive UI
* Dashboard Analytics
* Excel Report Generation

---

# 🎯 Future Enhancements

* AI-based Expense Categorization
* WhatsApp Receipt Upload
* Mobile Application
* Budget Notifications
* Multi-user Expense Sharing
* AI Financial Insights

---

# 📚 Learning Outcomes

This project strengthened my understanding of:

* Full Stack Development
* Spring Boot
* React
* JWT Authentication
* REST API Design
* OCR Integration
* MySQL
* Cloud Deployment
* Software Architecture
* Secure Application Development

---

# 👨‍💻 Author

**Phaneendra Guttula**

Software Developer Intern @ Infosys

📧 [phaniguttula25@gmail.com](mailto:phaniguttula25@gmail.com)

🔗 LinkedIn:
https://linkedin.com/in/phani-guttula

🌐 Portfolio:
https://phani-guttula.netlify.app

---

⭐ If you found this project interesting, consider giving it a star.
