# Secret Santa

This **Spring Boot** project manages the assignment of participants for a Secret Santa game. It includes the service layer, controllers, and entities required to generate and persist the assignments while applying specific constraints.
### Live Application
You can access the deployed application here:  
[http://secret-santa4-env.eba-e37udnrv.us-east-1.elasticbeanstalk.com/](http://secret-santa4-env.eba-e37udnrv.us-east-1.elasticbeanstalk.com/)

## Assignment Restrictions

The following constraints are applied during the Secret Santa assignment process:

1. **Not the Same Person**  
   Participants cannot be assigned to themselves.

2. **Not the Same Family**  
   Participants from the same immediate family (identified by `tempFamilyId`) cannot be assigned to each other if both family IDs are non-null.

3. **Receiver Not Already Used**  
   Each participant can only receive one gift, ensuring no duplicate assignments.

4. **Not Repeated in the Last 3 Years**  
   A participant cannot be assigned to the same receiver if the pair has already been assigned in the last three years.
## Important Business Considerations

- **Organizer Responsibility**
    1. The organizer is responsible for registering all participants in the Secret Santa.
    2. The organizer may also group the participants by immediate family (`tempFamilyId`) if necessary.

- **Immediate Family Definition**  
  The immediate family is the group that the Secret Santa organizer defines through the user interface, typically the nuclear family.
    - The immediate family relationship is identified in memory via the `tempFamilyId` attribute.
    - This relationship is **not** persisted in the database; it is only used in memory to discard invalid assignments (e.g., to avoid gifting within the same family).

- **Even Number of Participants**  
  The number of participants entered in the list must be **even**. If it is odd, the assignment process will throw an exception 400.

- **Email as the Unique Identifier**  
  Email is the persistent identifier for a participant across different years.
    - If the email is already found in the database, it implies the participant has been part of previous Secret Santa draws.
    - Each participant’s email is unique in the system.

## Requirements

- **Java 17** or higher
- **Maven** 3.8+ (or use the `mvnw` wrapper if provided)
- **Postgresql**

## Project Structure
- **controllers**: Contains the REST controllers (e.g., `SecretSantaController`).
- **services / services.impl**: Business logic (`SecretSantaServiceImpl`).
- **dto**: Data transfer objects (`ApiResponseDTO`, `ParticipantRequestDTO`, etc.).
- **entities**: JPA entities, such as `Participant` and `LogAssignment`.
- **mappers**
- **personalized exceptions**
- **filters**
- **repositories**: Spring Data JPA repositories.
- **util**
- **test**: JUnit and Mockito tests.

## Deployment with AWS Elastic Beanstalk and Amazon RDS

The application has been deployed to **AWS Elastic Beanstalk**, with the database hosted on **Amazon RDS (PostgreSQL)**. This architecture ensures scalability, reliability, and the ability to handle multiple concurrent users.

### Live Application
You can access the deployed application here:  
[http://secret-santa4-env.eba-e37udnrv.us-east-1.elasticbeanstalk.com/](http://secret-santa4-env.eba-e37udnrv.us-east-1.elasticbeanstalk.com/)

- The combination of **Elastic Beanstalk's auto-scaling capabilities** and the **managed PostgreSQL database on RDS** ensures the application can support multiple concurrent users efficiently.
- The load balancer distributes traffic evenly across the EC2 instances, preventing bottlenecks and improving response times.


## How to Build

1. Clone or download this repository.
2. Navigate to the project root (where the `pom.xml` file is located).
   Compile with Maven:

```bash
mvn clean install
```
- This command will download dependencies, build the project, and create a .jar file under target/

## How to Run

### Option 1: Using Maven Spring Boot Plugin

Run the application via:

```bash
mvn spring-boot:run
````

- The server will start on [http://localhost:8087](http://localhost:8087) by default, as configured in the `application.yml`.

## How to Test

Run all tests (unit and integration) using:

```bash
mvn test
````

## Main Endpoints

By default, the primary endpoint is:

- **POST** `/api/v1/secret-santa/generate/{year}`
  - **Description**: Generates Secret Santa assignments for the provided participant list.
  - **Body**: A JSON array of `ParticipantRequestDTO` objects, for example:
  ```json
  [
    {
      "name": "John Doe",
      "tempFamilyId": 1,
      "email": "john@example.com"
    },
    {
      "name": "Jane Doe",
      "tempFamilyId": 2,
      "email": "jane@example.com"
    }
  ]
  ```
  - **Response**: Returns an `ApiResponseDTO` containing the assignment results.

## Database Configuration

The application uses a PostgreSQL database. Ensure you have PostgreSQL installed and running with the following configurations,
you can use a **docker image of postgres** as well:

- **Database Name**: `secret_santa_db`
- **Username**: `postgres`
- **Password**: `postgres`
- **Port**: `5432` (default for PostgreSQL)

- The database connection settings are defined in the `application.yml` file


## Cliente Postman

There is a **collection attached** in the root of this project with examples of the differentes responses that the app can return

```bash
curl --location 'http://localhost:8087/api/v1/secret-santa/generate/2025' \
--header 'Content-Type: application/json' \
--data-raw '[
{
"name": "Alice Johnson",
"tempFamilyId": 1,
"email": "alice.johnson@example.com"
},
{
"name": "Bob Smith",
"tempFamilyId": 1,
"email": "bob.smith@example.com"
},
{
"name": "Charlie Brown",
"tempFamilyId": 2,
"email": "charlie.brown@example.com"
},
{
"name": "Diana Evans",
"tempFamilyId": 2,
"email": "diana.evans@example.com"
}
]
```

## Future Enhancements
- 1. Enhanced Immediate Family Management 
  - Implement persistent storage for tempFamilyId in the database to allow better tracking of family relationships across multiple years, rather than relying on in-memory processing.
- 2. Optimize Database Queries for Assignment Verification
  - Reduce the number of queries during the assignment process by optimizing the isNotRepeatedIn3Years method, for instance fetch all log_assignments from the last 3 years in a single query at the start of the process



