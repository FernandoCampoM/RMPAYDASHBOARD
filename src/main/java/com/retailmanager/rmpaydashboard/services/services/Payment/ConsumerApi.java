
package com.retailmanager.rmpaydashboard.services.services.Payment;

import com.google.gson.Gson;
import com.retailmanager.rmpaydashboard.exceptionControllers.exceptions.ConsumeAPIException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

public class ConsumerApi {
    public static String consumeAPI(String URL, Object body) throws ConsumeAPIException{
        String res = "";

        //Creamos el cliente de conexión al API Restful
        Client client = ClientBuilder.newBuilder().build();

        //Creamos el target lo cuál es nuestra URL junto con el nombre del método a llamar
        WebTarget target = client.target(URL);

        //Creamos nuestra solicitud que realizará el request
        Invocation.Builder solicitud = target.request();
        
        //add headres
        solicitud.header("Content-Type", "application/x-www-form-urlencoded");

        //Convertimos el objeto req a un json
        Gson gson = new Gson();
        String jsonString = gson.toJson(body);

        //Enviamos nuestro json vía post al API Restful
        Response post = solicitud.post(Entity.json(jsonString));

        //Recibimos la respuesta y la leemos en una clase de tipo String, en caso de que el json sea tipo json y no string, debemos usar la clase de tipo JsonObject.class en lugar de String.class
        String responseJson = post.readEntity(String.class);
        res = responseJson;

        //Imprimimos el status de la solicitud
        System.out.println("Estatus: " + post.getStatus());

        switch (post.getStatus()) { 
            case 200:
                res = responseJson;
                break;
            default:
                throw new ConsumeAPIException(res, post.getStatus()); 
        }


        //Imprimimos la respuesta del API Restful
        System.out.println(res);
        return res
        ;
    }
    public static String consumeAPIATHM(String URL, Object body, String bearerToken) throws ConsumeAPIException{
        String res = "";

        //Creamos el cliente de conexión al API Restful
        Client client = ClientBuilder.newClient();

        //Creamos el target lo cuál es nuestra URL junto con el nombre del método a llamar
        WebTarget target = client.target(URL);

        //Creamos nuestra solicitud que realizará el request
        Invocation.Builder solicitud = target.request();
        
        //add headres
        solicitud.header("Content-Type", "application/json");
        solicitud.header("Accept", "application/json");
        if(bearerToken!=null){
            solicitud.header("Authorization", "Bearer "+bearerToken);
        }
        //Convertimos el objeto req a un json
        Gson gson = new Gson();
        String jsonString = gson.toJson(body);

        //Enviamos nuestro json vía post al API Restful
        Response post = null;
        if(body!=null){
            post = solicitud.post(Entity.json(jsonString));
        }else{
            post = solicitud.post(null);
        }

        //Recibimos la respuesta y la leemos en una clase de tipo String, en caso de que el json sea tipo json y no string, debemos usar la clase de tipo JsonObject.class en lugar de String.class
        String responseJson = post.readEntity(String.class);
        res = responseJson;

        //Imprimimos el status de la solicitud
        System.out.println("Estatus: " + post.getStatus());

        switch (post.getStatus()) { 
            case 200:
                res = responseJson;
                break;
            case 202:
                res = responseJson;
                break;
            default:
                throw new ConsumeAPIException(res, post.getStatus()); 
        }


        //Imprimimos la respuesta del API Restful
        System.out.println(res);
        return res;
    }
}
