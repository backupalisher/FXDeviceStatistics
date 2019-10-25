package info.part4.Utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class GetPageHttps {
    public Document getPage(String link) throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        String html = null;
        try {
            URL url = new URL(link);
            URLConnection con = url.openConnection();
            con.setConnectTimeout(2000);
            Reader reader = new InputStreamReader(con.getInputStream());
            if (reader != null) {
                while (true) {
                    int ch = reader.read();
                    if (ch == -1) {
                        break;
                    }
                    html += String.valueOf((char) ch);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (html != null) {
            Document page = Jsoup.parse(html);
            return page;
        }
//        System.out.println(page);
//        Document page = Jsoup.connect(link)
//                .timeout(1000).validateTLSCertificates(false).get();
        return null;
    };
}
