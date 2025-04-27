package ru.lenok.server.utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;
import ru.lenok.common.models.LabWork;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Map;

public class JsonReader {
    private static final Logger logger = LoggerFactory.getLogger(JsonReader.class);
    public Hashtable<String, LabWork> loadFromJson(String filename) throws IOException {
        Hashtable<String, LabWork> map = new Hashtable<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .create();

            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                try {
                    String key = entry.getKey();
                    LabWork labWork;
                    try {
                        labWork = gson.fromJson(entry.getValue(), LabWork.class);
                    } catch (Exception e) {
                        logger.error("Ошибка в элементе с ключом " + key + ": данные некорректны, он не будет добавлен.");
                        continue;
                    }

                    if (labWork == null || !labWork.validate()) {
                        logger.error("Ошибка в элементе с ключом " + key + ": данные некорректны, он не будет добавлен.");
                        continue;
                    }

                    map.put(key, labWork);
                } catch (JsonParseException e) {
                    logger.error("Ошибка парсинга элемента с ключом " + entry.getKey() + ": " + e.getMessage());
                }
            }

        } catch (FileNotFoundException e) {
            throw new IOException("JSON файл не найден: " + filename, e);
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.error("Ошибка в JSON формате, коллекция будет очищена");
        }

        return map;
    }
}

