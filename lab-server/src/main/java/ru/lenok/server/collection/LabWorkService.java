package ru.lenok.server.collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.lenok.common.models.LabWork;
import ru.lenok.server.utils.LocalDateTimeAdapter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class LabWorkService {
    private Storage storage;
    private final String filename;

    public LabWorkService(Hashtable<String, LabWork> initialState, String filename) {
        this.storage = new Storage(initialState);
        this.filename = filename;
    }

    public Map<String, LabWork> getWholeMap(){
        return storage.getMap();
    }
    public String put(String key, LabWork lab) {
        String warning = null;
        if (storage.getMap().containsKey(key)) {
            warning = "ПРЕДУПРЕЖДЕНИЕ: элемент с таким ключом уже существовал, он будет перезаписан, ключ = " + key;
        }
        storage.put(key, lab);
        return warning;
    }
    public String getFileName(){
        return filename;
    }
    public void remove(String key) {
        storage.remove(key);
    }

    public int getCollectionSize() {
        return storage.length();
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

    /*   public String filterWithDescription(String descript_part) {
           StringBuilder answer = new StringBuilder("");
           for (String key : storage.getMap().keySet()) {
               LabWork labWork = storage.getMap().get(key);
               if (labWork.getDescription().contains(descript_part)) {
                   answer.append(key + " = " + labWork + "\n");
               }
           }
           return (answer.toString());
       }
   */
    public Map<String, LabWork> filterWithDescription(String descript_part) {
        return storage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getDescription().contains(descript_part))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }


    /*   public String filterWithName(String name_part) {
           StringBuilder answer = new StringBuilder("");
           List keys = new ArrayList<>(storage.getMap().keySet());
           for (int i = 0; i < keys.size(); i++) {
               if (storage.getMap().get(keys.get(i)).getName().startsWith(name_part)) {
                   answer.append(keys.get(i) + " = " + storage.getMap().get(keys.get(i)) + "\n");
               }
           }
           return (answer.toString());
       }
   */
    public Map<String, LabWork> filterWithName(String name_part) {
        return storage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getName().startsWith(name_part))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /*
        public void removeGreater(LabWork elem) {
            List keys = new ArrayList<>(storage.getMap().keySet());
            //       System.out.println(keys);
            for (int i = 0; i < keys.size(); i++) {
                //           System.out.println(storage.getMap().get(keys.get(i)).compareTo(elem));
                if (storage.getMap().get(keys.get(i)).compareTo(elem) > 0) {
                    storage.remove((String) keys.get(i));
                }
            }
            idCounter--;
        }
    */
/*
    public void removeGreater(LabWork elem) {
        storage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(elem) > 0)
                .forEach(entry -> storage.remove(entry.getKey()));
        idCounter--;
    }
*/
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

    /*    public String getKeyByLabWorkId(Long id) {
            for (String key : storage.getMap().keySet()) {
                if (storage.getMap().get(key).getId() == id) {
                    return key;
                }

            }
            throw new IllegalArgumentException("Нет элемента с таким id");
        }
    */
    public String getKeyByLabWorkId(Long id) {
        return storage.getMap().entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(id))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет элемента с таким id"));
    }

    /*   public String sortedByNameCollection(String arg) {
           List<LabWorkEntry> entryList = new ArrayList<>();
           for (Map.Entry<String, LabWork> mapEntry : getStorage().getMap().entrySet()) {
               entryList.add(new LabWorkEntry(mapEntry.getKey(), mapEntry.getValue()));

           }
           Collections.sort(entryList);
           StringBuilder result = new StringBuilder();
           for (LabWorkEntry labWorkEntry : entryList) {
               String key = labWorkEntry.key;
               result.append(key).append(" = ").append(labWorkEntry.labWork).append("\n");
           }
           return result.toString();
       }
   */
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
