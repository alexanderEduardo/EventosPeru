package com.alex.spring.security.demo.jobs;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class BackupJob {

    // Configura los parámetros de la base de datos
    private static final String DB_NAME = "db_eventosperu";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "12345";
    private static final String BACKUP_PATH = "C:/backups/eventosperu";

    @Scheduled(cron = "0 0 2 ? * FRI")
    public void backupDatabase() {
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String backupFileName = BACKUP_PATH + File.separator + "backup_" + date + ".sql";

        String mysqldumpPath = "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe";
        String command = String.format("cmd /c \"%s\" -u%s -p%s %s > %s", mysqldumpPath, DB_USER, DB_PASSWORD, DB_NAME, backupFileName);

        System.out.println("Comando ejecutado: " + command);

        try {
            Process process = Runtime.getRuntime().exec(command);

            // Capturar errores
            try (BufferedReader errorStream = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errorStream.readLine()) != null) {
                    System.err.println(line);
                }
            }

            int processComplete = process.waitFor();
            if (processComplete == 0) {
                System.out.println("Backup exitoso: " + backupFileName);
            } else {
                System.err.println("Error al realizar el backup.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}
