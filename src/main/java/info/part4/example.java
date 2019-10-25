package info.part4;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

public class example {
    private static final String filePath = "C:\\Temp\\json.json";

    public static void main(String[] args) {
        try {
            // считывание файла JSON
            FileReader reader = new FileReader(filePath);
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(reader);

            // получение строки из объекта
            String firstName = (String) jsonObject.get("firstname");
            System.out.println(firstName);

            // получение номера из объекта
            long id = (long) jsonObject.get("id");
            System.out.println(id);

            // получение массива
            JSONArray lang = (JSONArray) jsonObject.get("languages");

            // берем элементы массива
            for (int i = 0; i < lang.size(); i++) {
                System.out.println(lang.get(i));
            }
            Iterator i = lang.iterator();
            // берем каждое значение из массива json отдельно
            while (i.hasNext()) {
                JSONObject innerObj = (JSONObject) i.next();
                System.out.println(innerObj.get("lang") +
                        " " + innerObj.get("knowledge"));
            }
            // обрабатываем структуру в объекте
            JSONObject structure = (JSONObject) jsonObject.get("job");
            System.out.println("Into job structure, name: " + structure.get("name"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
