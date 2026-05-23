FROM debian:bookworm-slim

# Встановлюємо mosquitto та wget (для завантаження плагіна)
RUN apt-get update && apt-get install -y \
    mosquitto \
    wget \
    && rm -rf /var/lib/apt/lists/*

# Завантажуємо вже скомпільований плагін напряму з GitHub
RUN wget -qO /usr/lib/libmosquitto_jwt_auth.so https://github.com/wiomoc/mosquitto-jwt-auth/releases/download/0.4.0/libmosquitto_jwt_auth.so

# Створюємо необхідні директорії та віддаємо їх користувачу mosquitto (щоб не було помилок доступу)
RUN mkdir -p /mosquitto/data /etc/mosquitto/certs && \
    chown -R mosquitto:mosquitto /mosquitto /etc/mosquitto /usr/lib/libmosquitto_jwt_auth.so

# Перемикаємось на безпечного системного користувача
USER mosquitto

# Відкриваємо порти (1882 для IoT, 1883 для Quarkus)
EXPOSE 1882 1883

CMD ["mosquitto", "-c", "/etc/mosquitto/mosquitto.conf"]