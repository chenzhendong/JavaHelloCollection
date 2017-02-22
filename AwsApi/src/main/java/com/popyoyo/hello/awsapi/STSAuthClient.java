package com.popyoyo.hello.awsapi;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

/**
 * Created by Zhendong Chen on 2/21/17.
 */

public class STSAuthClient {

    public static void main(String[] args) {
        STSAuthClient client = new STSAuthClient();
        client.run();
    }


    private String readAll(URLConnection conn) throws Exception{
        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

        return response.toString();
    }

    // Start to run the server
    public void run() {
        try {
            // Create socket factory
            URL awsUrl = new URL("https://fstest.3m.com/idp/startSSO.ping?PartnerSpId=urn:amazon:webservices");
            HttpsURLConnection awsConn = (HttpsURLConnection) awsUrl.openConnection();

            awsConn.setReadTimeout(5000);
            awsConn.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
            awsConn.addRequestProperty("User-Agent", "Mozilla");

            // temporary to build request cookie header
            StringBuilder sb = new StringBuilder();

            // find the cookies in the response header from the first request
            List<String> cookies = awsConn.getHeaderFields().get("Set-Cookie");
            if (cookies != null) {
                for (String cookie : cookies) {
                    if (sb.length() > 0) {
                        sb.append("; ");
                    }

                    // only want the first part of the cookie header that has the value
                    String value = cookie.split(";")[0];
                    sb.append(value);
                }
            }
            // build request cookie header to send on all subsequent requests
            String cookieHeader = sb.toString();
            System.out.println(cookieHeader);

            System.out.println(awsConn.getResponseCode());
            System.out.println(awsConn.getResponseMessage());

            // start post username / password

            URL ssoUrl = new URL("https://enltest.3m.com/enl/login_servlet");
            HttpsURLConnection ssoConn = (HttpsURLConnection)ssoUrl.openConnection();

            ssoConn.setRequestMethod("POST");
            ssoConn.setRequestProperty("User-Agent", "Mozilla/5.0");
            ssoConn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            ssoConn.setRequestProperty("Cookie", cookieHeader);

            String urlParameters = "{'js_valid': 'false', 'enlcheck': 'y', 'ck_required': 'n', 'country': 'US', 'language': 'en', 'Destination': 'HTTPS://extraftest.3m.com/fstest/enl/login-redirect-page-XSSO.html?resumePath=/idp/resumeSAML20/idp/startSSO.ping', 'REQUEST_URL': '/enl/enl_display_servlet', 'userName': 'a4d98zz', 'passwd': '8Bear@Winter', 'login': 'Login', 'Register': 'Register'}";
            ssoConn.setRequestProperty("CONTENT_LENGTH", Integer.toString(urlParameters.length()));
            // Send post request
            ssoConn.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(ssoConn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            readAll(ssoConn);

            urlParameters = "{'pf.challengeResponse': '999999'}";

            System.out.print("Enter text passcode:");
            String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
            urlParameters.replace("999999", input);

            // Send post request
            ssoConn.setDoOutput(true);
            wr = new DataOutputStream(ssoConn.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();
            readAll(ssoConn);


        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

