import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.Gmail;
import java.util.Collections;

public class GmailService {

    // Buraya Playground'dan aldığın değerleri tek seferlik yapıştır
    private static final String CLIENT_ID = "716837879680-amsnjgds9fi5itgj11psl6bmganeoi5c.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "GOCSPX-SrsnUtJ1_GvztqAUGJta3kLwaTT";
    private static final String REFRESH_TOKEN = "1//04s8TdBnFt3xMCgYIARAAGAOSNwF-L9irp2qxdxnmbl_1N2M_F6Doou2CFDPD2kaIGcqOrH7HYZhkd2N1zn2Y7if0bApIeGpGAw0";

    public static Gmail getService() throws Exception {
        // Dosya veya DataStore kullanmadan doğrudan token üzerinden yetkilendirme
        Credential credential = new GoogleCredential.Builder()
                .setTransport(GoogleNetHttpTransport.newTrustedTransport())
                .setJsonFactory(GsonFactory.getDefaultInstance())
                .setClientSecrets(CLIENT_ID, CLIENT_SECRET)
                .build()
                .setFromTokenResponse(new TokenResponse().setRefreshToken(REFRESH_TOKEN));

        return new Gmail.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("GmailAutomationService")
                .build();
    }

    // Basit bir kullanım örneği:
    public static void main(String[] args) throws Exception {
        Gmail service = getService();
        String user = "me";
        var profile = service.users().getProfile(user).execute();
        System.out.println("Bağlantı Başarılı! Mail Adresi: " + profile.getEmailAddress());
    }
}
