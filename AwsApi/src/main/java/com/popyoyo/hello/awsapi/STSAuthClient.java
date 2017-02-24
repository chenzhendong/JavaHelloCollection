package com.popyoyo.hello.awsapi;


import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithSAMLRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleWithSAMLResult;
import com.amazonaws.services.securitytoken.model.Credentials;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Created by Zhendong Chen on 2/21/17.
 */

public class STSAuthClient {

    public static void main(String[] args) throws Exception {
        STSAuthClient client = new STSAuthClient();
        client.run();
    }

    private String getAssertion(String content) throws Exception {
        Document doc = Jsoup.parse(content);
        for (Element input : doc.select("input")) {
            if ("SAMLResponse".equals(input.attr("name"))) {
                return input.attr("value");
            }
        }
        throw new Exception("Cannot find SAML Token from Response html.");
    }

    private String getSaml(String assertion) throws Exception {
        return new String(Base64.getDecoder().decode(assertion), "utf-8");
    }

    private UrlEncodedFormEntity createPostData(String contnet) throws Exception {
        Document doc = Jsoup.parse(contnet);
        Element form = doc.select("form").first();
        if (form != null) {
            Elements inputs = form.select("input");
            List<NameValuePair> paras = new ArrayList<>();

            for (Element input : inputs) {
                String key = input.attr("name");
                String value = input.attr("value");
                if (key.contains("user")) {
                    System.out.print("Enter User Name:");
                    value = new BufferedReader(new InputStreamReader(System.in)).readLine();

                }
                if (key.contains("pass")) {
                    System.out.print("Enter Password:");
                    if(System.console() != null) {
                        value = new String(System.console().readPassword());
                    } else {
                        value = new BufferedReader(new InputStreamReader(System.in)).readLine();
                    }
                }
                paras.add(new BasicNameValuePair(key, value));
            }

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paras);
            return entity;
        } else {
            throw new Exception("Cannot find login form, something wrong on login page.");
        }
    }

    private UrlEncodedFormEntity createPostMfaCode(String content, String msg) throws Exception {
        Document doc = Jsoup.parse(content);
        Element form = doc.select("form").first();
        if (form != null) {
            Elements inputs = form.select("input");
            List<NameValuePair> paras = new ArrayList<>();

            for (Element input : inputs) {
                String key = input.attr("name");
                String value = input.attr("value");
                if (key.contains("pf.challengeRespons")) {
                    value = msg;
                }
                paras.add(new BasicNameValuePair(key, value));
            }

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paras);
            return entity;
        } else {
            throw new Exception("Cannot find text message input box, must be something wrong on prev steps.");
        }

    }

    public void run() {
        BasicCookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build();

        try {
            HttpGet httpget = new HttpGet("https://fstest.3m.com/idp/startSSO.ping?PartnerSpId=urn:amazon:webservices");
            CloseableHttpResponse res1 = httpclient.execute(httpget);

            HttpEntity entity = res1.getEntity();
            UrlEncodedFormEntity postData = createPostData(EntityUtils.toString(entity));

            EntityUtils.consume(entity);

            HttpUriRequest req2 = RequestBuilder.post()
                    .setUri(new URI("https://enltest.3m.com/enl/login_servlet"))
                    .setEntity(postData)
                    .build();
            CloseableHttpResponse res2 = httpclient.execute(req2);

            HttpUriRequest req3 = RequestBuilder.post()
                    .setUri(new URI("https://fstest.3m.com/idp/resumeSAML20/idp/startSSO.ping"))
                    .setEntity(postData)
                    .build();

            CloseableHttpResponse res3 = httpclient.execute(req3);

            String res3String = EntityUtils.toString(res3.getEntity());

            //System.out.println(res3String);
            while (res3String.contains("pf.challengeRespons")) {
                System.out.print("Enter text passcode:");
                String text = new BufferedReader(new InputStreamReader(System.in)).readLine();
                req3 = RequestBuilder.post()
                        .setUri(new URI("https://fstest.3m.com/idp/resumeSAML20/idp/startSSO.ping"))
                        .setEntity(createPostMfaCode(res3String, text))
                        .build();
                res3 = httpclient.execute(req3);
                res3String = EntityUtils.toString(res3.getEntity());
            }
            //System.out.println(res3String);
            String assertion = getAssertion(res3String);
            String saml = getSaml(assertion);

            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            org.w3c.dom.Document dom = factory.newDocumentBuilder().parse(new InputSource(new StringReader(saml)));
            XPath xPath = XPathFactory.newInstance().newXPath();
            NodeList nl = (NodeList) xPath.compile("//Attribute[@Name='https://aws.amazon.com/SAML/Attributes/Role']/AttributeValue")
                    .evaluate(dom, XPathConstants.NODESET);
            String[] roles = nl.item(0).getTextContent().split(",");
            String role_arn = roles[0];
            String principal_arn = roles[1];

            AWSSecurityTokenServiceClient client = new AWSSecurityTokenServiceClient();
            AssumeRoleWithSAMLRequest samlRequest = new AssumeRoleWithSAMLRequest();
            samlRequest.setRoleArn(role_arn);
            samlRequest.setPrincipalArn(principal_arn);
            samlRequest.setSAMLAssertion(assertion);
            AssumeRoleWithSAMLResult tokenResult = client.assumeRoleWithSAML(samlRequest);
            Credentials credentials = tokenResult.getCredentials();
            String accessId = credentials.getAccessKeyId();
            String accessKey = credentials.getSecretAccessKey();
            String sessionToken = credentials.getSessionToken();

            System.out.println();
            System.out.println("AccessID: " + accessId);
            System.out.println("AccessKey: " + accessKey);
            System.out.println("Token: " + sessionToken);

            httpclient.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


}

