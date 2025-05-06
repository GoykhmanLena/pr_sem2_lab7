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

    public String getWholeMap(){
        return memoryStorage.getCollectionAsString();
        //return labWorkDAO.selectAll();
    }

    public String put(String key, LabWork lab) throws SQLException {
        synchronized (memoryStorage) {
            if (memoryStorage.containsKey(key)) {
                throw new IllegalArgumentException("Ошибка: элемент с таким ключом уже существует, ключ = " + key);
            }
            Long elemId = labWorkDAO.insert(key, lab);
            lab.setId(elemId);
            memoryStorage.put(key, lab);
            return "";
        }
    }

    public void remove(String key) throws SQLException {
        synchronized (memoryStorage) {
            labWorkDAO.delete(key);
            memoryStorage.remove(key);
        }
    }

    public int getCollectionSize(){
        return memoryStorage.length();
        //return labWorkDAO.countAll();
    }

    public void clearCollection(long ownerId) throws SQLException {
        synchronized (memoryStorage) {
            labWorkDAO.deleteForUser(ownerId);
            memoryStorage.deleteForUser(ownerId);
        }
    }

 /*   public String getCollectionAsJson() throws IOException {
        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        String json = gson.toJson(getMemoryStorage().getMap());
        return json;
    }
*/
    public String filterWithDescription(String descript_part) {
        return memoryStorage.filterWithDescription(descript_part);
    }

    public String filterWithName(String name_part) {
        return memoryStorage.filterWithName(name_part);
    }

    public void removeGreater(LabWork elem, long userId) throws SQLException {
        synchronized (memoryStorage) {
            List<String> keysForRemoving = memoryStorage.keysOfGreater(elem, userId);

            labWorkDAO.deleteByKeys(keysForRemoving);
            keysForRemoving.forEach(key -> memoryStorage.remove(key));
        }
    }


    public void replaceIfGreater(String key, LabWork newLabWork) throws SQLException {
        synchronized (memoryStorage) {
            if (memoryStorage.comparing(key, newLabWork)) {
                checkAccess(newLabWork.getOwnerId(), key);
                newLabWork.setId(memoryStorage.getId(key));
                labWorkDAO.updateById(key, newLabWork);
                memoryStorage.put(key, newLabWork);
            }
        }
    }

    public void updateByLabWorkId(Long id, LabWork labWork) throws SQLException {
        synchronized (memoryStorage) {
            String key = memoryStorage.getKeyByLabWorkId(id);
            checkAccess(labWork.getOwnerId(), key);
            labWork.setId(id);
            labWorkDAO.updateById(key, labWork);
            memoryStorage.put(key, labWork);
        }
    }

    public String toString() {
        return memoryStorage.toString();
    }

    public void checkAccess(Long currentUserId, String key){
        memoryStorage.checkAccess(currentUserId, key);
    }
}
