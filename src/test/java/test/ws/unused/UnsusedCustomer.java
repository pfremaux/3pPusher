package test.ws.unused;

import io.vertx.core.json.JsonObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @deprecated
 */
public final class UnsusedCustomer {

    private final String USER_AGENT = "Mozilla/5.0";

    public static void main(String[] args) throws Exception {

        UnsusedCustomer http = new UnsusedCustomer();

        /*System.out.println("Testing 1 - Send Http GET request");
        http.sendGet();*/

        Thread.sleep(5000);

        System.out.println("\nTesting 2 - Send Http POST request");
        http.sendPost("", "");

    }


    // HTTP POST request
    public void sendPost(String destUuid, String msg) throws Exception {
        String protocol = "http";
        String url = "://localhost:8080/newComer";
        URL obj = new URL(protocol + url);
        HttpURLConnection con = null;
        if ("http".equalsIgnoreCase(protocol)) {
            con = (HttpURLConnection) obj.openConnection();
        } else if ("https".equalsIgnoreCase(protocol)) {
            con = (HttpsURLConnection) obj.openConnection();
        }

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("id", msg);
        msgMap.put("dest", destUuid);
        JsonObject jsonObject = new JsonObject(msgMap);
        String urlParameters = jsonObject.encode();

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(this.getClass().getSimpleName() + " : " + response.toString());

    }


    // HTTP GET request
    public void sendGet() throws Exception {

        String url = "http://www.google.com/search?q=mkyong";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }

}
