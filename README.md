# Task Management System    [![Task Management CI](https://github.com/lurldgbodex/task-management-system/actions/workflows/task_management_ci.yaml/badge.svg)](https://github.com/lurldgbodex/task-management-system/actions/workflows/task_management_ci.yaml)
A backend API for managing tasks, setting deadlines, and marking tasks as completed. This project focuses on robust error handling, secure user authentication, and scalability to support a growing number of users and tasks efficiently.

## Table of Contents
1. [Features](#features)
2. [Project Structure](#project-structure)
3. [Getting Started](#getting-started)
4. [Running the API](#running-the-api)
5. [API Endpoints](#api-endpoints)
6. [Error Handling](#error-handling)
7. [Authentication](#authentication)
8. [Scalability](#scalability)
9. [License](#license)

## Features
- Task Management: Create, Update, Delete and Retrieve tasks.
- Deadlines: Assign deadlines to tasks for effective tracking
- Task Completion: Mark tasks as completed and filter by completion status.
- Authentication: Secure login and authentication to protect user data and tasks.

## Project Structure
To be added.

## Getting Started
### Prerequisites
1. Java 17+
2. Maven for dependency managment
3. MySQL as primary database
4. Git for version control

### Setup Instructions
1. Clone the repository:
```bash
git clone https://github.com/lurldgbodex/task-management-system.git
cd task-management-system
```
2. Configure Environment Variables.
Create a .env file in the root directory and specify the required environment variables like below.
```env
DATABASE_URL=jdbc:mysql://localhost:3306/taskdb
DATABASE_USERNAME=your-datbase-username
DATABASE_PASSWORD=your-database-password
```
3. Install dependencies
```bash
mvn install
```
4. Set up Database:
 - make sure mysql is running.
 - create a new database named `taskdb` (or as specified in your .env)


## Running The API
1. Start the Application
    ```bash
   mvn spring-boot:run
    ```
2. Access the API:
    The API should be running on http://localhost:8000. You can test the endpoints using a tool like postman or curl.

## API Endpoints
|   HTTP Method     |   Endpoint    |   Description     |
|-------------------|---------------|-------------------|

## Error Handling
The API provides descriptive error messages for common issues, such as:
- 400 Bad Request: Missing or invalid input
- 401 Unauthorized: Access denied due to missing or invalid token
- 404 Not Found: Task or resources not found

## Authentication
The API uses JWT(JSON Web Tokens) for authentication:
- login: users login using their email and password and receive a jwt
- Authentication Endpoints: Include the token in the Authorization header as Bearer <token> for authorized actions.

### Example Authorization Header
```
Authorization: Bearer your_jwt_token
```

## Scalability
To ensure that the API can handle increased loads:
<To Be Added>

## License
This project is licensed under the MIT License. see the [LICENSE](#license) file for more information