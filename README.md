<div align="center">
  <img src="user_avatar.png" alt="Interview Mentor Logo" width="120" />

  # 🎓 Interview Mentor
  
  **Your Intelligent & Adaptive Companion for Tech Interview Success.**

  [![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)](https://www.java.com/)
  [![SQLite](https://img.shields.io/badge/SQLite-07405E?style=for-the-badge&logo=sqlite&logoColor=white)](https://www.sqlite.org/)
  [![Spring Boot](https://img.shields.io/badge/Spring_Boot-F2F4F9?style=for-the-badge&logo=spring-boot)](https://spring.io/projects/spring-boot)
  [![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)](https://reactjs.org/)
</div>

<br />

## 🌟 Overview

**Interview Mentor** is a comprehensive, full-stack application designed to help aspiring software engineers and professionals prepare for technical interviews. It combines a robust **Java Swing Desktop Application** with a **Spring Boot REST API** and a **React-based Web Frontend**, providing a seamless, multi-platform study experience.

Whether you're brushing up on Data Structures, algorithms, System Design, or Behavioral questions, Interview Mentor tracks your progress, adapts to your weaknesses, and keeps you motivated through gamification.

---

## ✨ Key Features

- 🧠 **Adaptive Learning Engine**: Automatically adjusts question difficulty based on your performance history.
- 🔁 **Spaced Repetition System (SRS)**: Optimize memory retention for flashcards and tricky concepts.
- 📊 **Deep Analytics & Heatmaps**: Visualize your study streaks, category mastery, and accuracy trends with beautiful interactive charts.
- 🎮 **Gamification & Achievements**: Earn badges, maintain study streaks, and stay motivated.
- ☁️ **Cloud Sync**: Seamlessly sync your progress and bookmarks across devices via the REST API.
- 🔐 **OAuth Integration**: Quick and secure login using Google and GitHub.
- 📄 **PDF Export**: Generate detailed performance reports and customized training plans.
- 📚 **Comprehensive Question Bank**: Covers Algorithms, Data Structures, OOP, DBMS, OS, Computer Networks, and Behavioral interviews.

---

## 🛠️ Technology Stack

### Desktop Application (Client)
- **Language**: Java 17+
- **GUI Framework**: Java Swing (with custom 2D painting for modern aesthetics)
- **Local Database**: SQLite

### Backend Services
- **Framework**: Spring Boot (Java)
- **Architecture**: RESTful API
- **Features**: User Management, Quiz Session tracking, Cloud Sync

### Web Frontend
- **Framework**: React.js with Vite
- **Styling**: Modern CSS / Tailwind (Responsive & Accessible)

---

## 🚀 Getting Started

### Prerequisites

Ensure you have the following installed on your local machine:
- **Java Development Kit (JDK) 17** or higher
- **Maven** (for building the Java applications)
- **Node.js** (v16+) and **npm** (for the web frontend)

### Installation & Execution

#### 1. Running the Java Desktop App
Navigate to the project root and use the Maven wrapper to compile and run the application:
```bash
./mvnw clean compile exec:java -Dexec.mainClass="com.interviewmentor.App"
```
*(Make sure to set up your own OAuth credentials in `OAuthService.java` if you wish to use Google/GitHub login).*

#### 2. Running the Spring Boot Backend
Navigate to the `backend` directory and start the Spring Boot server:
```bash
cd backend
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8080`.

#### 3. Running the Web Frontend
Navigate to the `frontend` directory, install dependencies, and start the Vite dev server:
```bash
cd frontend
npm install
npm run dev
```
The web app will be accessible at `http://localhost:5173`.

---

## 📂 Project Structure

```text
InterviewMentor/
├── src/main/java/com/interviewmentor/   # Java Desktop Client App
│   ├── model/           # Data structures & Entities
│   ├── service/         # Business logic (OAuth, Analytics, SRS)
│   ├── view/            # Swing UI Panels & Custom Components
│   └── util/            # Helpers & API Clients
├── backend/             # Spring Boot REST API
│   └── src/main/java/com/interviewmentor/backend/
│       ├── controller/  # API Endpoints
│       ├── model/       # JPA Entities
│       └── repository/  # Database access
├── frontend/            # React.js Web Application
│   ├── src/             # Components, Styles, Hooks
│   └── package.json     # Node dependencies
├── interview_mentor.db  # Local SQLite database (Auto-generated)
└── pom.xml              # Maven configuration
```

---

## 🤝 Contributing

Contributions are welcome! If you'd like to improve the app or add new features:
1. Fork the repository.
2. Create your feature branch (`git checkout -b feature/AmazingFeature`).
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4. Push to the branch (`git push origin feature/AmazingFeature`).
5. Open a Pull Request.

---

## 📜 License

This project is licensed under the MIT License. Feel free to use, modify, and distribute it as per the license terms.

<br />

<div align="center">
  <i>Built with ❤️ to help you ace your next interview!</i>
</div>
