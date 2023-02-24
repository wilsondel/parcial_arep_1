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
            } else if (path.contains("unaryInvoke")) {
                responsePart+= unaryInvokeImplementation(command,responsePart);
            } else if (path.contains("binaryInvoke")) {
                responsePart+= binaryInvokeImplementation(command,responsePart);
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

    private static String binaryInvokeImplementation(String command, String responsePart) throws ClassNotFoundException {

        String javaMethod = command.substring(13).replace(")","");
        String[] myInvoke = javaMethod.split(",");
        String myClass = myInvoke[0];
        String myMethod = myInvoke[1];
        String myType = myInvoke[2];
        String myValue = myInvoke[3];
        String myType2 = myInvoke[4];
        String myValue2 = myInvoke[5];

        System.out.println("class:  " + myClass + " + myMethod: " + myMethod );
        System.out.println("myType:  " + myType + " + myValue: " + myValue );


        Class[] myDataType = new Class[2];
        myDataType[0] = returnClass(myType);
        myDataType[1] = returnClass(myType2);

        // metodo para tipos de argumentos

        Class c = Class.forName(myClass);
        Method m = null;
        String response = "";
        try {
            m = c.getDeclaredMethod(myMethod,myDataType);
            if(myType.equals("int")) {
                response = String.valueOf(m.invoke(null,Integer.valueOf(myValue),Integer.valueOf(myValue2)));
            } else if (myType.equals("String")) {
                response = String.valueOf(m.invoke(null,myValue,myValue2));
            } else if (myType.equals("double")) {
                response = String.valueOf(m.invoke(null, Double.valueOf(myValue),myValue2));
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        responsePart += String.valueOf(response);


        return responsePart;

    }

    private static Class returnClass(String myType) {

        Class<String> s = String.class;
        Class<Integer> i = int.class;
        Class<Double> d = Double.class;

        if(myType.equals("int")) {
            return i;
        } else if (myType.equals("String")) {
            return s;
        } else if (myType.equals("double")) {
            return d;

        }

        return String.class;


    }

    private static String unaryInvokeImplementation(String command, String responsePart) throws NoSuchMethodException, ClassNotFoundException {

        String javaMethod = command.substring(12).replace(")","");
        String[] myInvoke = javaMethod.split(",");
        String myClass = myInvoke[0];
        String myMethod = myInvoke[1];
        String myType = myInvoke[2];
        String myValue = myInvoke[3];

        System.out.println("class:  " + myClass + " + myMethod: " + myMethod );
        System.out.println("myType:  " + myType + " + myValue: " + myValue );

        Class<String> s = String.class;
        Class<Integer> i = int.class;
        Class<Double> d = Double.class;
        

        Class[] iList = new Class[1];
        iList[0] = i;
        Class[] sList = new Class[1];
        sList[0] = s;
        Class[] dList = new Class[1];
        dList[0] = d;


        Class c = Class.forName(myClass);
        Method m = null;
        String response = "";
        try {
            if(myType.equals("int")) {
                m = c.getDeclaredMethod(myMethod,iList);
                response = String.valueOf(m.invoke(null,Integer.valueOf(myValue)));

            } else if (myType.equals("String")) {
                m = c.getDeclaredMethod(myMethod,sList);
                response = String.valueOf(m.invoke(null,myValue));
            } else if (myType.equals("double")) {
                m = c.getDeclaredMethod(myMethod,dList);
                response = String.valueOf(m.invoke(null, Double.valueOf(myValue)));
            }

        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        responsePart += String.valueOf(response);


        return responsePart;
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