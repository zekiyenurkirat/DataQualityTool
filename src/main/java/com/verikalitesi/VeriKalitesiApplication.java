package com.verikalitesi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * main metodu burada, uygulama buradan başlıyor.
 *
 * DataSourceAutoConfiguration'ı kapattım çünkü spring-boot-starter-jdbc
 * eklenince Spring, uygulama açılırken hazır bir veritabanı bağlantısı
 * bekliyor; application.properties'te bulamazsa hata verip açılmıyor.
 * Benim projemde öyle sabit bir veritabanı adresi yok, kullanıcı hangi
 * veritabanına bağlanacağını daha sonra, arayüzden kendisi girecek.
 * O yüzden Spring'in bunu otomatik kurmasına izin vermedim, bağlantıyı
 * kendi yazdığım DatabaseConnectionService, kullanıcı bilgileri
 * girdiğinde kuracak.
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class VeriKalitesiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VeriKalitesiApplication.class, args);
    }

}
