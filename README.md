-> Relational Database Management: Architected a scalable e-commerce backend utilizing PostgreSQL, Spring Data JPA, and Hibernate ORM for complex multi-table entity mappings.

-> Atomic Transactions: Engineered a robust checkout pipeline using @Transactional boundaries to guarantee strict inventory deduction and prevent phantom stock logical bugs.

-> Authentication & Authorization: Implemented stateless user authentication and Role-Based Access Control (RBAC) securing administrative endpoints using Spring Security 6 and JSON Web Tokens (JWT).

-> Advanced Search Capabilities: Designed a dynamic product search engine by writing custom JPQL queries, allowing clients to combine optional keyword, price, and category filters seamlessly.

-> API Standardization & Error Handling: Enforced the Data Transfer Object (DTO) pattern to prevent proxy crashes and built a global @RestControllerAdvice interceptor for uniform JSON error handling.

-> Architecture: Developed using Java 22 and Spring Boot 3, utilizing Apache Maven for dependency management and deployed on an embedded Apache Tomcat server.

-> Setup Instructions: Please refer to the application-dev.yml file for the necessary database credentials, JWT secret keys, and configuration required for the system to run locally.