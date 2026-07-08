package com.retailmanager.rmpaydashboard.utils;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.Random;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.retailmanager.rmpaydashboard.models.Terminal;

public class Utils {
    public static void main(String[] args){
        String texto="12345678";
        String textoCodificado=new BCryptPasswordEncoder().encode(texto);
        System.out.println(textoCodificado);
        System.out.println(new BCryptPasswordEncoder().matches("rmpayuser", "$2a$10$3hXD2CTLL18GTNFCZiYfWuWyhMQFc30EvsMh5fWnCIXKPfflhe/mC"));
        LocalDate date = LocalDate.now();
        LocalDate date2 = LocalDate.now();
        System.out.println("La fecha es: "+date.isEqual(date2));
        System.out.println("Terminal Id: "+getTerminalId());
        
    }
    private static String getTerminalId() {
        Random random = new Random();
        //long currentTimeMillis = System.currentTimeMillis();
        //long generatedId = currentTimeMillis + randomInt;
        Terminal terminal = null;
        int randomInt=0;
        
            randomInt = random.nextInt(99999); // Agrega una aleatoriedad para reducir colisiones
            
            randomInt=23;
            String formattedInt = String.format("%05d", randomInt);

        return "RM"+formattedInt;
    }
    public static long generateUniqueId() {
         Random random = new Random();
        long currentTimeMillis = System.currentTimeMillis();
        int randomInt = random.nextInt(1000); // Agrega una aleatoriedad para reducir colisiones
        return currentTimeMillis + randomInt; // Combina el tiempo y el número aleatorio
    }
    public static String generateSafeFileName(String originalFileName) {
        // Normalizar el nombre de archivo para eliminar caracteres especiales o no ASCII
        String normalizedFileName = Normalizer.normalize(originalFileName, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
        
        // Eliminar caracteres no permitidos en nombres de archivos
        normalizedFileName = normalizedFileName.replaceAll("[/\\\\:*?\"<>| ]", "_");
        
        // Limitar la longitud del nombre de archivo si es necesario
        int maxLength = 255; // Longitud máxima permitida por la mayoría de los sistemas de archivos
        if (normalizedFileName.length() > maxLength) {
            normalizedFileName = normalizedFileName.substring(0, maxLength);
        }
        
        return normalizedFileName;
    }

}
