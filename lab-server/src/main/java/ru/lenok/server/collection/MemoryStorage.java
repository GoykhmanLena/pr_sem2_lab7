package ru.lenok.server.collection;

import lombok.Data;
import ru.lenok.common.models.LabWork;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//@Data
public class MemoryStorage {
    private final Hashtable<String, LabWork> map;
    private final Object monitor;

    public MemoryStorage(Hashtable<String, LabWork> map) {
        this.map = map;
        monitor = this.map;
    }

    public Object getMonitor(){
        return monitor;
    }

    public void put(String key, LabWork lab) {
        map.put(key, lab);
    }

    public void remove(String key) {
        map.remove(key);
    }

    public int length() {
        return map.size();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (String key : map.keySet()) {
            LabWork labWork = map.get(key);
            result.append(key).append(" = ").append(labWork).append("\n");
        }
        return result.toString();
    }

    public void clear() {
        map.clear();
    }
    public String getCollectionAsString(){
        return sortMapAndStringify(this.map);
    }

    public String sortMapAndStringify(Map<String, LabWork> filteredMap) {
        return filteredMap.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue()) // сортировка по значению
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public boolean containsKey(String key){
        return map.containsKey(key);
    }

    public String filterWithDescription(String descript_part){
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().getDescription().contains(descript_part))
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public String filterWithName(String name_part){
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().getName().startsWith(name_part))
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining("\n"));
    }

    public List<String> keysOfGreater(LabWork elem, long userId){
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().getOwnerId().equals(userId))
                .filter(entry -> entry.getValue().compareTo(elem) > 0)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public boolean comparing(String key, LabWork newLabWork){
        return map.get(key).compareTo(newLabWork) < 0;
    }
    public Long getId(String key){
        return map.get(key).getId();
    }

    public String getKeyByLabWorkId(Long id) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(id))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет элемента с таким id"));
    }

    public LabWork getLabWorkById(Long id) {
        return map.entrySet().stream()
                .filter(entry -> entry.getValue().getId().equals(id))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Нет элемента с таким id"));
    }

    public void checkAccess(Long currentUserId, String key){
        LabWork labWork = map.get(key);
        if (labWork != null){
            if (!labWork.getOwnerId().equals(currentUserId)){
                throw new IllegalArgumentException("Доступ запрещен. Попытка изменения чужой записи.");
            }
        }
    }

    public void deleteForUser(long ownerId){
        Iterator<Map.Entry<String, LabWork>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, LabWork> entry = iterator.next();
            LabWork labWork = entry.getValue();
            if (labWork.getOwnerId().equals(ownerId)) {
                iterator.remove();
            }
        }
    }
}
