# RecipeBox — Personal Recipe and Meal Planner

A normal Spring Boot + Maven + MySQL project with a small HTML/CSS/JavaScript frontend.

## Requirements

- JDK 17
- IntelliJ IDEA
- Maven (IntelliJ can use the Maven wrapper/embedded Maven)
- MySQL 8

## 1. Create the database

Open MySQL Workbench or MySQL command line:

```sql
CREATE DATABASE recipebox;
```

No tables need to be created manually. JPA creates/updates them when the application starts.

## 2. Configure MySQL

Open:

`src/main/resources/application.properties`

Change:

```properties
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

to your local MySQL root password.

If your MySQL username is not `root`, change that too.

## 3. Open in IntelliJ

1. Extract the ZIP.
2. Open the extracted `RecipeBox` folder in IntelliJ IDEA.
3. IntelliJ should detect `pom.xml` as a Maven project.
4. Make sure Project SDK is JDK 17.
5. Wait for Maven dependencies to finish downloading.
6. Run `RecipeBoxApplication.java`.

The application runs at:

`http://localhost:8080`

The frontend is served by Spring Boot itself, so there is no separate frontend server to start.

## 4. Frontend

Open:

`http://localhost:8080`

The UI supports:

- Add/edit recipes
- Ingredients, quantities and units
- Search by recipe title or cuisine
- Favorite recipes
- Weekly meal planning
- One planned meal per day
- Combined weekly shopping list

## 5. Browser-only testing

This project does not require Postman. Run the Spring Boot application from IntelliJ and open `http://localhost:8080`. The web interface calls the backend automatically and stores data in MySQL.

To verify the saved data directly in MySQL Workbench:

```sql
USE recipebox;
SELECT * FROM users;
SELECT * FROM recipes;
SELECT * FROM ingredients;
SELECT * FROM recipe_ingredients;
SELECT * FROM meal_plans;
```

## 6. Normal IntelliJ run process

1. Open this folder in IntelliJ.
2. Let Maven finish importing.
3. Set Project SDK to JDK 17.
4. Create the `recipebox` database in MySQL.
5. Put your MySQL password in `application.properties`.
6. Run `RecipeBoxApplication.java`.
7. Open `http://localhost:8080`.
8. Add recipes from the browser.

## Database entities

- `users`
- `recipes`
- `ingredients`
- `recipe_ingredients`
- `meal_plans`

The extra `recipe_ingredients` table stores the quantity/unit of an ingredient for each recipe. This is needed because the same ingredient can have different quantities in different recipes.

## Business rules implemented

1. A meal plan must point to an existing recipe.
2. A user can only plan their own recipe.
3. A user cannot create two meal plans for the same date.
4. Weekly shopping quantities are aggregated in the service layer.
5. Invalid requests return readable JSON errors instead of stack traces.

## Suggested Postman testing order

1. POST `/api/users`
2. GET `/api/users`
3. POST `/api/recipes?userId=1`
4. GET `/api/recipes?userId=1`
5. POST `/api/meal-plans?userId=1`
6. GET `/api/meal-plans?userId=1&date=2026-09-28`
7. GET `/api/meal-plans/shopping-list?userId=1&date=2026-09-28`
8. Try creating another meal for the same date to verify the business-rule error.
