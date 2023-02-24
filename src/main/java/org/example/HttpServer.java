package org.example;


import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.io.*;

public class HttpServer {

    public static void main(String[] args) throws IOException, ClassNotFoundException, NoSuchMethodException {

        while(true) {
            ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(35000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }

        Socket clientSocket = null;
        try {
            System.out.println("Listo para recibir ...");
            clientSocket = serverSocket.accept();
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }
        PrintWriter out = new PrintWriter(
                clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()));
        String inputLine, outputLine;

        String path="";
        boolean firstLine = true;
        while ((inputLine = in.readLine()) != null) {
            if(firstLine && inputLine.contains("HTTP/1.1")) {
                path = inputLine.split(" ")[1];
                firstLine = false;
            }
            System.out.println("Recibí: " + inputLine);
            if (!in.ready()) {break; }
        }

        System.out.println("INPUT LINE: " + path);

        String responsePart = "<ul>";
        if (path.contains("/consulta?comando=")) {
            String command = path.substring(18);
            if(path.contains("Class")) {
                responsePart+= classImplementation(command,responsePart);
            } else if(path.contains("invoke")) {
                responsePart+= invokeImplementation(command,responsePart);

            }
        }

        responsePart+="</ul>";



            outputLine = "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n" + html(responsePart);
        out.println(outputLine);

        out.close();
        in.close();

        clientSocket.close();
        serverSocket.close();
        }


    }

    private static String invokeImplementation(String command, String responsePart) throws ClassNotFoundException, NoSuchMethodException {

        String javaMethod = command.substring(7).replace(")","");
        String[] myInvoke = javaMethod.split(",");
        String myClass = myInvoke[0];
        String myMethod = myInvoke[1];

        System.out.println("class:  " + myClass + " + myMethod: " + myMethod );
        Class c = Class.forName(myClass);
        Method m = c.getDeclaredMethod(myMethod);

        String response = "";
        try {
            response = String.valueOf(m.invoke(null));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        responsePart += response;

        return responsePart;
    }

    private static String classImplementation(String command, String responsePart) throws ClassNotFoundException {
        String javaMethod = command.substring(6).replace(")","");
        Class c = Class.forName(javaMethod);
        Method[] m = c.getDeclaredMethods();
        Field[] f = c.getDeclaredFields();

        for(Method method : m) {
//                    System.out.println("Method: " + method.getName());
            responsePart += "<li>" + method.getName() + "</li>";
        }

        for(Field fields : f) {
//                    System.out.println("Fields: " + fields.getName());
            responsePart += "<li>" + fields.getName() + "</li>";

        }

        return responsePart;
    }


    public static String html(String responsePart){
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <title>Form Example</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "<body>\n" +
                "<h1>Name of the class</h1>\n" +
                "<form action=\"/consulta\">\n" +
                "    <label for=\"comando\">comando:</label><br>\n" +
                "    <input type=\"text\" id=\"comando\" name=\"comando\" value=\"Class(java.lang.Math)\"><br><br>\n" +
                "    <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "</form>\n" +
                "<div id=\"getrespmsg\">" + responsePart +"</div>\n" +
                "\n" +
                "<script>\n" +
                "        function loadGetMsg() {\n" +
                "            let nameVar = document.getElementById(\"comando\").value;\n" +
                "            const xhttp = new XMLHttpRequest();\n" +
                "            xhttp.onload = function() {\n" +
                "                document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                this.responseText;\n" +
                "            }\n" +
                "            xhttp.open(\"GET\", \"/consulta?comando=\"+nameVar);\n" +
                "            xhttp.send();\n" +
                "        }\n" +
                "</script>\n" +
                "</body>\n" +
                "</html>";
    }


}