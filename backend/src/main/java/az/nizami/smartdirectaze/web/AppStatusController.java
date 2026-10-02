package az.nizami.smartdirectaze.web;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Public health and version of the running app: used by deploy/deploy.sh, deploy/healthcheck.sh and by hand.
 * Lives at the old WhatsApp "alive" address, which nginx already passes through.
 */
@RestController
@Log4j2
public class AppStatusController {

    // Build time is stored in UTC; shown in Baku time
    private static final DateTimeFormatter BAKU_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss xxx").withZone(ZoneId.of("Asia/Baku"));

    // Missing when the app is started from the IDE without a Maven build
    private final BuildProperties build;
    private final DataSource dataSource;

    public AppStatusController(ObjectProvider<BuildProperties> build, DataSource dataSource) {
        this.build = build.getIfAvailable();
        this.dataSource = dataSource;
    }

    @GetMapping("${app.url.component.webhook.w}${app.url.component.alive}")
    public ResponseEntity<Map<String, String>> alive() {
        boolean databaseUp = databaseUp();
        Map<String, String> status = new LinkedHashMap<>();
        status.put("status", databaseUp ? "UP" : "DOWN");
        status.put("version", build != null ? build.getVersion() : "dev");
        status.put("commit", build != null && build.get("commit") != null ? build.get("commit") : "local");
        status.put("built", build != null && build.getTime() != null ? BAKU_TIME.format(build.getTime()) : "-");
        status.put("database", databaseUp ? "UP" : "DOWN");
        // 503 when the database is gone: deploy and health checks treat the app as not working
        return ResponseEntity.status(databaseUp ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(status);
    }

    private boolean databaseUp() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(2);
        } catch (Exception e) {
            log.warn("Database check failed: {}", e.getMessage());
            return false;
        }
    }
}
