package ru.lenok.server.collection;

import lombok.Data;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.daos.DBConnector;
import ru.lenok.server.daos.LabWorkDAO;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

@Data
public class LabWorkService {
    private MemoryStorage memoryStorage;
    private final LabWorkDAO labWorkDAO;
    private final Object monitor;
    private final DataSource ds;

    public LabWorkService(LabWorkDAO labWorkDAO, DBConnector dbConnector) throws SQLException {
        this.labWorkDAO = labWorkDAO;
        ds = dbConnector.getDatasource();
        this.memoryStorage = new MemoryStorage(new Hashtable<>(labWorkDAO.selectAll()));
        monitor = memoryStorage.getMonitor();
    }

    public String getWholeMap() {
        synchronized (monitor) {
            return memoryStorage.getCollectionAsString();
        }
    }

    public String put(String key, LabWork lab) throws SQLException {
        synchronized (monitor) {
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
        synchronized (monitor) {
            labWorkDAO.delete(key);
            memoryStorage.remove(key);
        }
    }

    public int getCollectionSize() {
        synchronized (monitor) {
            return memoryStorage.length();
        }
    }

    public void clearCollection(long ownerId) throws SQLException {
        synchronized (monitor) {
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
    public String filterWithDescription(String descriptPart) {
        synchronized (monitor) {
            return memoryStorage.filterWithDescription(descriptPart);
        }
    }

    public String filterWithName(String namePart) {
        synchronized (monitor) {
            return memoryStorage.filterWithName(namePart);
        }
    }

    public void removeGreater(LabWork elem, long userId) throws SQLException {
        synchronized (monitor) {
            List<String> keysForRemoving = memoryStorage.keysOfGreater(elem, userId);
            labWorkDAO.deleteByKeys(keysForRemoving);
            keysForRemoving.forEach(key -> memoryStorage.remove(key));
        }
    }


    public void replaceIfGreater(String key, LabWork newLabWork) throws SQLException {
        synchronized (monitor) {
            try (Connection connection = ds.getConnection()) {
                if (memoryStorage.comparing(key, newLabWork)) {
                    checkAccess(newLabWork.getOwnerId(), key);
                    newLabWork.setId(memoryStorage.getId(key));
                    labWorkDAO.updateById(key, newLabWork, connection);
                    memoryStorage.put(key, newLabWork);
                }
            }
        }
    }

    public void updateByLabWorkId(Long id, LabWork labWork) throws SQLException {
        try (Connection connection = ds.getConnection()) {
            updateByLabWorkIdWithConnection(id, labWork, connection);
        }
    }

    public void updateByLabWorkIdWithConnection(Long id, LabWork labWork, Connection connection) throws SQLException {
        synchronized (monitor) {
            String key = memoryStorage.getKeyByLabWorkId(id);
            checkAccess(labWork.getOwnerId(), key);
            labWork.setId(id);
            labWorkDAO.updateById(key, labWork, connection);
            memoryStorage.put(key, labWork);
        }
    }

    public void checkAccess(Long currentUserId, String key) {
        synchronized (monitor) {
            memoryStorage.checkAccess(currentUserId, key);
        }
    }

    public LabWork getLabWorkById(Long labWorkId) {
        synchronized (monitor) {
            return memoryStorage.getLabWorkById(labWorkId);
        }
    }
}
