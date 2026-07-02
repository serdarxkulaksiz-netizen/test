import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import java.util.List;

public class GmailReader {

    // Kendi değerlerin
    private static final String CLIENT_ID = "716837879680-amsnjgds9fi5itgj11psl6bmganeoi5c.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "GOCSPX-SrsnUtJ1_GvztqAUGJta3kLwaTT";
    private static final String REFRESH_TOKEN = "1//04s8TdBnFt3xMCgYIARAAGAOSNwF-L9irp2qxdxnmbl_1N2M_F6Doou2CFDPD2kaIGcqOrH7HYZhkd2N1zn2Y7if0bApIeGpGAw0";

    // 1. Gmail Servisini Hazırla
    private Gmail getService() throws Exception {
        Credential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .build()
                .setFromTokenResponse(new TokenResponse().setRefreshToken(REFRESH_TOKEN));

        return new Gmail.Builder(GoogleNetHttpTransport.newTrustedTransport(), 
                                 GsonFactory.getDefaultInstance(), credential)
                .setApplicationName("GmailAutomation")
                .build();
    }

    // 2. En son gelen mailin konusunu oku
    public String getLatestEmailSubject() throws Exception {
        Gmail service = getService();
        
        // Gelen kutusunda en son 1 maili listele
        var response = service.users().messages().list("me")
                .setQ("label:INBOX")
                .setMaxResults(1L)
                .execute();

        List<Message> messages = response.getMessages();
        if (messages == null || messages.isEmpty()) {
            return null;
        }

        // Mesajın detaylarını çek
        Message message = service.users().messages().get("me", messages.get(0).getId()).execute();

        // Konuyu (Subject) başlıklar arasından bul
        return message.getPayload().getHeaders().stream()
                .filter(h -> h.getName().equalsIgnoreCase("Subject"))
                .findFirst()
                .map(h -> h.getValue())
                .orElse("Konusuz");
    }
}
