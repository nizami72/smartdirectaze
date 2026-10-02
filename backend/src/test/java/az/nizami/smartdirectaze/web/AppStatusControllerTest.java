package az.nizami.smartdirectaze.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.info.BuildProperties;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class AppStatusControllerTest {

    @SuppressWarnings("unchecked")
    private AppStatusController controller(BuildProperties build, DataSource dataSource) {
        ObjectProvider<BuildProperties> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(build);
        return new AppStatusController(provider, dataSource);
    }

    @Test
    void showsVersionAndCommitWhenDatabaseIsUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("version", "0.1.0");
        props.setProperty("commit", "abc1234");
        props.setProperty("time", "2026-10-02T05:49:00Z");
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(true);

        var response = controller(new BuildProperties(props), dataSource).alive();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("0.1.0", response.getBody().get("version"));
        assertEquals("abc1234", response.getBody().get("commit"));
        assertEquals("2026-10-02 09:49:00 +04:00", response.getBody().get("built"));
    }

    @Test
    void returns503WhenDatabaseIsDown() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("down"));

        var response = controller(null, dataSource).alive();

        assertEquals(503, response.getStatusCode().value());
        assertEquals("DOWN", response.getBody().get("database"));
        assertEquals("dev", response.getBody().get("version"));
    }
}
