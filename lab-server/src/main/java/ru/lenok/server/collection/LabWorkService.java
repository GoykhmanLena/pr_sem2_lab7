package ru.lenok.server.collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.daos.LabWorkDAO;
import ru.lenok.server.utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class LabWorkService {
    private MemoryStorage memoryStorage;
    private final LabWorkDAO labWorkDAO;
    public LabWorkService(LabWorkDAO labWorkDAO) throws SQLException {
        this.labWorkDAO = labWorkDAO;
        this.memoryStorage = new MemoryStorage(new Hashtable<>(labWorkDAO.selectAll()));
    }

    public Map<String, LabWork> getWholeMap() throws SQLException{
        return memoryStorage.getMap();
        //return labWorkDAO.selectAll();
    }

    public String put(String key, LabWork lab) throws SQLException {
        if (memoryStorage.getMap().containsKey(key)) {
            throw new IllegalArgumentException("Ошибка: элемент с таким ключом уже существует, ключ = " + key);
        }
        labWorkDAO.insert(key, lab);
        memoryStorage.put(key, lab);
        return "";
    }

    public void remove(String key) throws SQLException {
        labWorkDAO.delete(key);
        memoryStorage.remove(key);
    }

    public int getCollectionSize() throws SQLException {
        return memoryStorage.length();
        //return labWorkDAO.countAll();
    }

    public void clearCollection(long ownerId) throws SQLException {
        labWorkDAO.deleteForUser(ownerId);
        memoryStorage.clear();//TODO удалить только у пользователя
    }

    public String getCollectionAsJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        String json = gson.toJson(getMemoryStorage().getMap());
        return json;
    }

    public Map<String, LabWork> filterWithDescription(String descript_part) {
        return memoryStorage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getDescription().contains(descript_part))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Map<String, LabWork> filterWithName(String name_part) {
        return memoryStorage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getName().startsWith(name_part))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void removeGreater(LabWork elem, long userId) throws SQLException {
        List<String> keysForRemoving = memoryStorage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getOwnerId().equals(userId))
                .filter(entry -> entry.getValue().compareTo(elem) > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        labWorkDAO.deleteByKeys(keysForRemoving);
        keysForRemoving.forEach(key -> memoryStorage.remove(key));
    }


    public void replaceIfGreater(String key, LabWork newLabWork) throws SQLException {
        LabWork oldLabWork = memoryStorage.getMap().get(key);
        if (oldLabWork.compareTo(newLabWork) < 0) {
            checkAccess(newLabWork.getOwnerId(), key);
            newLabWork.setId(oldLabWork.getId());
            labWorkDAO.updateById(key, newLabWork);
            memoryStorage.put(key, newLabWork);
        }
    }

    public void updateByLabWorkId(Long id, LabWork labWork) throws SQLException {
        String key = getKeyByLabWorkId(id);
        checkAccess(labWork.getOwnerId(), key);
        labWork.setId(id);
        labWorkDAO.updateById(key, labWork);
        memoryStorage.put(key, labWork);
    }

    public String toString() {
        return memoryStorage.toString();
    }

    public String getKeyByLabWorkId(Long id) {
        return memoryStorage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(id))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет элемента с таким id"));
    }

    public String sortedByNameCollection() {
        return getMemoryStorage().getMap().entrySet().stream()
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

    public void checkAccess(Long currentUserId, String key){
        LabWork labWork = memoryStorage.getMap().get(key);
        if (labWork != null){
            if (!labWork.getOwnerId().equals(currentUserId)){
                throw new IllegalArgumentException("Доступ запрещен. Попытка изменения чужой записи.");
            }
        }
    }
}
