package com.example.urlshortener;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class AbstractIntegrationTest {

    
    static final MySQLContainer<?> MY_SQL_CONTAINER;

    
    static final GenericContainer<?> REDIS_CONTAINER;

    
    static final GenericContainer<?> ZOOKEEPER_CONTAINER;

    static {
        
        MY_SQL_CONTAINER = new MySQLContainer<>("mysql:8.0")
                .withDatabaseName("test_db")
                .withUsername("test")
                .withPassword("test");
        MY_SQL_CONTAINER.start();

        
        REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:alpine"))
                .withExposedPorts(6379);
        REDIS_CONTAINER.start();

        
        
        ZOOKEEPER_CONTAINER = new GenericContainer<>(DockerImageName.parse("zookeeper:3.9.3"))
                .withExposedPorts(2181)
                .withEnv("ZOO_4LW_COMMANDS_WHITELIST", "*") 
                .waitingFor(Wait.forListeningPort()); 
        ZOOKEEPER_CONTAINER.start();
    }

    
    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        
        registry.add("spring.datasource.url", MY_SQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MY_SQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MY_SQL_CONTAINER::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop"); 

        
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);

        
        
        String zkConnectString = ZOOKEEPER_CONTAINER.getHost() + ":" + ZOOKEEPER_CONTAINER.getFirstMappedPort();
        registry.add("zookeeper.connection-string", () -> zkConnectString);
    }
}