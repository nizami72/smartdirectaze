package az.nizami.smartdirectaze.whatsapp.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.net.SocketTimeoutException;

import static org.junit.jupiter.api.Assertions.*;

// Green API failures are logged with a clear cause, but never with the token from the URL
class GreenApiErrorTextTest {

    private static final String TOKEN = "secret0123456789abcdef";

    @Test
    void httpStatusWithHint() {
        var e = HttpClientErrorException.create(HttpStatusCode.valueOf(466), "", null, null, null);
        assertEquals("HTTP 466 (instance limit or tariff exceeded)", GreenApiClient.describe(e));
        assertEquals("HTTP 401 (wrong idInstance or apiTokenInstance)",
                GreenApiClient.describe(HttpClientErrorException.create(HttpStatusCode.valueOf(401), "", null, null, null)));
    }

    @Test
    void networkErrorWithoutTheUrl() {
        // Spring writes the full request URL, token included, into I/O error messages
        var e = new ResourceAccessException("I/O error on POST request for \"https://api.green-api.com/waInstance7107/sendMessage/"
                + TOKEN + "\": Read timed out", new SocketTimeoutException("Read timed out"));

        String text = GreenApiClient.describe(e);

        assertEquals("network error: SocketTimeoutException", text);
        assertFalse(text.contains(TOKEN));
    }
}
