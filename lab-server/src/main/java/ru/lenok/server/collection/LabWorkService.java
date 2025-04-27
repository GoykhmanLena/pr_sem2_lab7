package ru.lenok.server.collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class LabWorkService {
    private Storage storage;
    private final LabWorkDAO labWorkDAO;
    public LabWorkService(Hashtable<String, LabWork> initialState, String dbHost, String dbPort, String dbUser, String dbPassword) throws SQLException {
       // this.storage = new Storage(initialState);
        labWorkDAO =new LabWorkDAO(initialState, dbHost, dbPort, dbUser, dbPassword);
    }

    public Map<String, LabWork> getWholeMap() throws SQLException{
       // return storage.getMap();
        return labWorkDAO.selectAll();
    }

    public String put(String key, LabWork lab) {
        String warning = null;
        if (storage.getMap().containsKey(key)) {
            warning = "ПРЕДУПРЕЖДЕНИЕ: элемент с таким ключом уже существовал, он будет перезаписан, ключ = " + key;
        }
        storage.put(key, lab);
        return warning;
    }

    public void remove(String key) {
        storage.remove(key);
    }

    public int getCollectionSize() throws SQLException {
       // return storage.length();
        return labWorkDAO.countAll();
    }

    public void clear_collection() {
        storage.clear();
    }

    public String getCollectionAsJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        String json = gson.toJson(getStorage().getMap());
        return json;
    }

    public Map<String, LabWork> filterWithDescription(String descript_part) {
        return storage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getDescription().contains(descript_part))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, LabWork> filterWithName(String name_part) {
        return storage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getName().startsWith(name_part))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void removeGreater(LabWork elem) {
        storage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(elem) > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList())
                .forEach(storage::remove);
    }


    public void replaceIfGreater(String key, LabWork newLabWork) {
        LabWork oldLabWork = storage.getMap().get(key);
        if (oldLabWork.compareTo(newLabWork) < 0) {
            newLabWork.setId(oldLabWork.getId());
            storage.put(key, newLabWork);
        }
    }

    public void updateByLabWorkId(Long id, LabWork labWork) {
        String key = getKeyByLabWorkId(id);
        labWork.setId(id);
        storage.put(key, labWork);
    }

    public String toString() {
        return storage.toString();
    }

    public String getKeyByLabWorkId(Long id) {
        return storage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(id))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет элемента с таким id"));
    }

    public String sortedByNameCollection() {
        return getStorage().getMap().entrySet().stream()
                .map(entry -> new LabWorkEntry(entry.getKey(), entry.getValue()))
                .sorted()
                .map(labWorkEntry -> labWorkEntry.key + " = " + labWorkEntry.labWork)
                .collect(Collectors.joining("\n"));
    }

    public static String sortMapAndStringify(Map<String, LabWork> filteredMap) {
        return filteredMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue()) // сортировка по значению
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    @AllArgsConstructor
    private static class LabWorkEntry implements Comparable<LabWorkEntry> {
        String key;
        LabWork labWork;

        @Override
        public int compareTo(LabWorkEntry labWorkEntry) {
            return this.labWork.getName().compareTo(labWorkEntry.labWork.getName());
        }
    }
}
