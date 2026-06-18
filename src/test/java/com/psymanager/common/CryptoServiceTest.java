package com.psymanager.common;

import com.psymanager.config.AppProperties;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoServiceTest {

    private CryptoService serviceWithKey() {
        // Chave AES de 32 bytes (Base64) apenas para teste.
        String key = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes());
        AppProperties props = new AppProperties("admin@test.com", key, null, null);
        return new CryptoService(props);
    }

    @Test
    void encryptThenDecryptRoundTrips() {
        CryptoService crypto = serviceWithKey();
        String plain = "1//refresh-token-secreto";

        String encrypted = crypto.encrypt(plain);

        assertThat(encrypted).isNotEqualTo(plain);
        assertThat(crypto.decrypt(encrypted)).isEqualTo(plain);
    }

    @Test
    void encryptProducesDifferentCipherTextEachTime() {
        CryptoService crypto = serviceWithKey();

        // IV aleatório => mesmo texto gera ciphertexts diferentes (mas ambos decifram igual).
        String first = crypto.encrypt("mesmo-valor");
        String second = crypto.encrypt("mesmo-valor");

        assertThat(first).isNotEqualTo(second);
        assertThat(crypto.decrypt(first)).isEqualTo("mesmo-valor");
        assertThat(crypto.decrypt(second)).isEqualTo("mesmo-valor");
    }
}
