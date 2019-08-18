package org.majki.intellij.ldapbrowser.ldap;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class LdapConnectionTrustManager implements X509TrustManager {

    private LdapConnectionInfo connectionInfo;
    private TrustManager[] trustManagers;

    public LdapConnectionTrustManager(LdapConnectionInfo connectionInfo, TrustManager[] trustManagers) {
        this.connectionInfo = connectionInfo;
        this.trustManagers = trustManagers;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                ((X509TrustManager) trustManager).checkClientTrusted(chain, authType);
            }
        }
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                try {
                    ((X509TrustManager) trustManager).checkClientTrusted(chain, authType);
                } catch (CertificateException e) {
                    if (chain.length == 1) {
                        markUntrustedConnection(chain[0], e);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    private void markUntrustedConnection(X509Certificate certificate, CertificateException e) throws CertificateException {
        String fingerprint;
        try {
            fingerprint = createFingerprint(certificate);
        } catch (NoSuchAlgorithmException ex) {
            throw new CertificateException("Could not create certificate SHA fingerprint", ex);
        }
        if (!fingerprint.equals(connectionInfo.getTrustedCertificateFingerprint())) {
            connectionInfo.setUntrustedCertificate(certificate, fingerprint);
            throw e;
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        for (TrustManager trustManager : trustManagers) {
            if (trustManager instanceof X509TrustManager) {
                return ((X509TrustManager) trustManager).getAcceptedIssuers();
            }
        }
        return new X509Certificate[0];
    }

    public static String createFingerprint(X509Certificate certificate) throws CertificateEncodingException, NoSuchAlgorithmException {
        byte[] encoded;
        MessageDigest messageDigest;
        encoded = certificate.getEncoded();
        messageDigest = MessageDigest.getInstance("SHA");
        byte[] digest = messageDigest.digest(encoded);
        return new HexBinaryAdapter().marshal(digest);
    }
}
