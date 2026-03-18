package dev.nez.producer.security;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.io.FileInputStream;
import java.io.InputStream;

@ApplicationScoped
public class MqttTrustManagerProvider {

    @ConfigProperty(name = "mqtt.broker.ca-path", defaultValue = "../secrets/tls/ca.crt")
    String caPath;

    private TrustManagerFactory tmf;

    public TrustManagerFactory provide() {
        return tmf;
    }

    @PostConstruct
    void init() {
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, null);

            try (InputStream is = new FileInputStream(caPath)) {
                X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);
                ks.setCertificateEntry("ca", caCert);

                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(ks);
                this.tmf = tmf;
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load CA certificate for TLS", e);
        }
    }
}
