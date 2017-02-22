package com.popyoyo.hello.awsapi;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by Zhendong Chen on 2/22/17.
 */

public class TrustAllManager implements X509TrustManager {
    public boolean isClientTrusted(X509Certificate[] chain) {
        return true;
    }

    public boolean isServerTrusted(X509Certificate[] x509Certificates) {
        return true;
    }

    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    public boolean isHostTrusted(X509Certificate[] chain) {
        return true;
    }
}

