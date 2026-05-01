plugins {
    id("java")
    id("application")
}

group = "com.promo"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // PostgreSQL драйвер
    implementation("org.postgresql:postgresql:42.7.1")

    // Пул соединений HikariCP
    implementation("com.zaxxer:HikariCP:5.0.1")

    // JSON обработка (Gson)
    implementation("com.google.code.gson:gson:2.10.1")

    // Хэширование паролей BCrypt
    implementation("at.favre.lib:bcrypt:0.10.2")

    // JWT токены
    implementation("com.auth0:java-jwt:4.4.0")

    // Логирование
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // HTTP клиент для Telegram API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // JavaMail для email
    implementation("com.sun.mail:jakarta.mail:2.0.1")

    // Для работы с .env файлами
    implementation("io.github.cdimascio:dotenv-java:3.0.0")

    // Тестирование
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("com.promo.otp.Application")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}