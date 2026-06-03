package com.retailmanager.rmpaydashboard.backgroundRoutines;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.springframework.context.ConfigurableApplicationContext;

public class BackgroundRoutines extends Thread{
    BackgroundRoutinesService backgroundRoutinesService ;
    public BackgroundRoutines(ConfigurableApplicationContext context){
        backgroundRoutinesService=context.getBean(BackgroundRoutinesService.class);
    }

    public void run(){
        System.out.println("ROUTINES IN BACKGROUND STARTED");
        //System.out.println("Se eliminarán aquellos registros que lleven más de 6 meses despues de la fecha de finalización");
        //Obtener fecha y hora actual
         Calendar now = Calendar.getInstance();
         // Establece la hora y el minuto en que deseas que se dispare el evento
        int hour = now.get(Calendar.HOUR_OF_DAY);   // Hora en formato de 24 horas
        int minute = now.get(Calendar.MINUTE); // Minutos
        // Calcula la próxima fecha en que se debe disparar el evento
        Calendar nextExecutionTime = Calendar.getInstance();
        nextExecutionTime.set(Calendar.HOUR_OF_DAY, hour);
        nextExecutionTime.set(Calendar.MINUTE, minute+2);
        if (nextExecutionTime.before(now) || nextExecutionTime.equals(now)) {
            // Si la hora programada ya ha pasado hoy, suma un día para la próxima ejecución
            nextExecutionTime.add(Calendar.DATE, 1);
        }

        // Calcula la diferencia en milisegundos entre la próxima ejecución y la hora actual
        long initialDelay = nextExecutionTime.getTimeInMillis() - now.getTimeInMillis();

        // Crea un objeto TimerTask para ejecutar tu evento
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                System.out.println("ROUTINES IN BACKGROUND RUNNING:"+hour+":"+minute+" "+now.toString());
                // Coloca aquí el código que deseas que se ejecute en el evento diario
                backgroundRoutinesService.deactivateExpiredTerminals();

                backgroundRoutinesService.priorNotificaionEmail();
                backgroundRoutinesService.lastDayNotificaionEmail();
                backgroundRoutinesService.afterNotificaionEmail();
                backgroundRoutinesService.automaticPayments();
                //AQUI LAS RUTINAS
            }
        };

        // Crea un Timer y programa el TimerTask para que se ejecute todos los días a la misma hora
        Timer timer = new Timer();
        timer.schedule(task, initialDelay, 24* 60 * 60 * 1000); // 24 * 60 * 60 * 1000 =24 horas en milisegundos

        // Espera indefinidamente para que el programa no finalice inmediatamente
        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
