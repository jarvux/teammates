package teammates.logic.external;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.multipart.FormDataMultiPart;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import teammates.common.util.EmailSendingStatus;
import teammates.common.util.EmailWrapper;
import teammates.common.util.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;

/**
 * Email sender service provided by Mailgun.
 *
 * @see <a href="https://cloud.google.com/appengine/docs/standard/java11/sending-messages#mailgun">https://cloud.google.com/appengine/docs/standard/java11/sending-messages#mailgun</a>
 * @see FormDataMultiPart
 */
public class SESService implements EmailSenderService {

    private static final Logger log = Logger.getLogger();

    /**
     * {@inheritDoc}
     */
    @Override
    public StringEntity parseToEmail(EmailWrapper wrapper) {
        try {
            String msg = new ObjectMapper().writeValueAsString(wrapper);
            String data = new ObjectMapper().writeValueAsString(new ApiMessage(msg));
            return new StringEntity(data);
        } catch (JsonProcessingException | UnsupportedEncodingException e ) {
            log.severe("Error parsing EmailWrapper to JSON", e);
            throw new RuntimeException("Error parsing EmailWrapper to JSON", e);
        }
    }

    @Override
    public EmailSendingStatus sendEmail(EmailWrapper wrapper) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("https://gu4wk78mnb.execute-api.us-east-2.amazonaws.com/test/message");
            httpPost.setEntity(parseToEmail(wrapper));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            ResponseHandler<EmailSendingStatus> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                String body = EntityUtils.toString(response.getEntity());
                if (status >= 200 && status < 300) {
                    return body != null ? new EmailSendingStatus(HttpStatus.SC_OK, body) : new EmailSendingStatus(HttpStatus.SC_OK, null);
                } else {
                    return body != null ? new EmailSendingStatus(status, body) : new EmailSendingStatus(status, null);
                }
            };
            return httpclient.execute(httpPost, responseHandler);
        } catch (IOException e) {
            log.severe("Error calling the API", e);
            throw new RuntimeException("Error parsing EmailWrapper to JSON", e);
        }
    }
}

class ApiMessage implements Serializable {
    private String msg;
    ApiMessage(String msg){
        this.msg = msg;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
}
