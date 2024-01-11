package org.keydash.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@RestController
public class HealthCheck {

    @Autowired
    private DataSource dataSource;

    @CrossOrigin(origins = "*")
    @GetMapping("/health/live")
    public ResponseEntity<String> livenessCheck() {
        // Implement your health check logic here
        boolean isLive = checkLiveness();

        if (isLive) {
            return ResponseEntity.ok("Live");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Not live");
        }
    }

    private boolean checkLiveness() {
        // For example, check database connectivity
        // Return true if healthy, false if not

        try (Connection connection = dataSource.getConnection()) {
//            System.out.println("Healthcheck liveness passed");
            return true;
        } catch (SQLException e) {
            // Log the exception details
            return false;
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/health/ready")
    public ResponseEntity<String> readinessCheck() {
        // Implement your health check logic here
        boolean isReady = checkReadiness();

        if (isReady) {
//            System.out.println("Healthcheck readiness passed");
            return ResponseEntity.ok("Ready");
        } else {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Not ready");
        }
    }

    private boolean checkReadiness() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            if (resultSet.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            // Log the exception for debugging
            System.out.println(e.toString());
            return false;
        }
    }

}
