package ru.lenok.server.collection;

import lombok.Data;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.daos.DBConnector;
import ru.lenok.server.daos.LabWorkDAO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.List;

@Data
public class LabWorkService {
    private MemoryStorage memoryStorage;
    private final LabWorkDAO labWorkDAO;
    private final Object monitor;
    private final Connection connection;

    public LabWorkService(LabWorkDAO labWorkDAO, DBConnector dbConnector) throws SQLException {
        this.labWorkDAO = labWorkDAO;
        this.memoryStorage = new MemoryStorage(new Hashtable<>(labWorkDAO.selectAll()));
        monitor = memoryStorage.getMonitor();
        connection = dbConnector.getConnection();
    }

    public String getWholeMap() {
        synchronized (monitor) {
            return memoryStorage.getCollectionAsString();
        }
    }

    public String put(String key, LabWork lab) throws SQLException {
        synchronized (monitor) {
            try {
                if (memoryStorage.containsKey(key)) {
                    throw new IllegalArgumentException("Ошибка: элемент с таким ключом уже существует, ключ = " + key);
                }
                Long elemId = labWorkDAO.insert(key, lab);
                connection.commit();
                lab.setId(elemId);
                memoryStorage.put(key, lab);
                return "";
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public void remove(String key) throws SQLException {
        synchronized (monitor) {
            try {
                labWorkDAO.delete(key);
                connection.commit();
                memoryStorage.remove(key);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    public int getCollectionSize() {
        synchronized (monitor) {
            return memoryStorage.length();
        }
    }

    public void clearCollection(long ownerId) throws SQLException {
        synchronized (monitor) {
            try {
                labWorkDAO.deleteForUser(ownerId);
                connection.commit();
                memoryStorage.deleteForUser(ownerId);
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
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
            try {
                List<String> keysForRemoving = memoryStorage.keysOfGreater(elem, userId);

                labWorkDAO.deleteByKeys(keysForRemoving);
                connection.commit();
                keysForRemoving.forEach(key -> memoryStorage.remove(key));
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }


    public void replaceIfGreater(String key, LabWork newLabWork) throws SQLException {
        synchronized (monitor) {
            long idForRestore = newLabWork.getId();
            try {
                if (memoryStorage.comparing(key, newLabWork)) {
                    checkAccess(newLabWork.getOwnerId(), key);
                    newLabWork.setId(memoryStorage.getId(key));
                    labWorkDAO.updateById(key, newLabWork);
                    connection.commit();
                    memoryStorage.put(key, newLabWork);
                }
            } catch (SQLException e) {
                newLabWork.setId(idForRestore);
                connection.rollback();
                throw e;
            }
        }
    }

    public void updateByLabWorkId(Long id, LabWork labWork, boolean needCommit) throws SQLException {
        synchronized (monitor) {
            try {
                String key = memoryStorage.getKeyByLabWorkId(id);
                checkAccess(labWork.getOwnerId(), key);
                labWork.setId(id);
                labWorkDAO.updateById(key, labWork);
                if (needCommit){
                    connection.commit();
                }
                memoryStorage.put(key, labWork);
            } catch (SQLException e) {
                if (needCommit) {
                    connection.rollback();
                }
                throw e;
            }
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
