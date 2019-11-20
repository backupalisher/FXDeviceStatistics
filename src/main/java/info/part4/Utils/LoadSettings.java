package info.part4.Utils;

import info.part4.Controller;

import java.io.*;

public class LoadSettings {
    public static void settings() {
        try {
            File file = new File("settings");
            FileInputStream fileInputStream = new FileInputStream(file.getAbsolutePath());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

            String strLine;
            String[] subStr;

            while ((strLine = bufferedReader.readLine()) != null) {
                subStr = strLine.split(";");
                switch (subStr[0].trim()) {
                    case ("URL_CLIENT_ENDPOINT"):
                        Controller.URL_CLIENT_ENDPOINT = subStr[1].trim();
                        break;
//                    case ("USER_ID"):
//                        Controller.USER_ID = Integer.parseInt(subStr[1].trim());
//                        break;
                    case ("COMPANY_ID"):
                        Controller.COMPANY_ID = Integer.parseInt(subStr[1].trim());
                        break;
                }
            }
//            System.out.println(Controller.URL_CLIENT_ENDPOINT + ", " + Controller.USER_ID + ", " + Controller.COMPANY_ID);
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }
}
