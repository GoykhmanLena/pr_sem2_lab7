package ru.lenok.common.models;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Data
public class LabWork implements Comparable<LabWork>, Serializable {
    private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private double minimalPoint; //Значение поля должно быть больше 0
    private String description; //Длина строки не должна быть больше 2863, Поле не может быть null
    private Difficulty difficulty; //Поле не может быть null
    private Discipline discipline; //Поле не может быть null

    public LabWork(String name, Coordinates coordinates, double minimalPoint, String description, Difficulty difficulty, Discipline discipline) {
        this.name = name;
        this.coordinates = coordinates;
        this.minimalPoint = minimalPoint;
        this.description = description;
        this.difficulty = difficulty;
        this.discipline = discipline;

        this.creationDate = LocalDateTime.now();
        this.id = 0L;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "{ id: " + id + ", name: " + name + ", coordinates: (" + coordinates.getX() + "; " +
                coordinates.getY() + "), creationDate: " + getFormattedCreationDate() + ", minimalPoint: " + minimalPoint +
                ", description: " + description + ", difficulty: " + difficulty + ", name and hours of discipline: " +
                discipline.getName() + " " + discipline.getPracticeHours() + " }";
    }

    private String getFormattedCreationDate() {
        return creationDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }
    public boolean validate() {
        return coordinates != null && coordinates.validate() &&
                discipline != null && discipline.validate() &&
                name != null && !name.equals("") &&
                creationDate != null &&
                minimalPoint > 0 &&
                description != null && description.length() <= 2863 &&
                difficulty != null && discipline != null;
    }

    @Override
    public int compareTo(LabWork lab) {
        return this.getName().compareTo(lab.getName());
    }

    public static class Builder {
        private Long id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
        private String name; //Поле не может быть null, Строка не может быть пустой
        private Coordinates coordinates; //Поле не может быть null
        private java.time.LocalDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
        private double minimalPoint; //Значение поля должно быть больше 0
        private String description; //Длина строки не должна быть больше 2863, Поле не может быть null
        private Difficulty difficulty; //Поле не может быть null
        private Discipline discipline; //Поле не может быть null

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setMinimalPoint(double minimalPoint) {
            if (minimalPoint <= 0) {
                throw new IllegalArgumentException("Значение поля должно быть положительным, где вы видели отрицательные баллы?");
            }
            this.minimalPoint = minimalPoint;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setDifficulty(Difficulty difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder setDisciplineName(String disciplineName) {
            if (this.discipline == null) {
                this.discipline = new Discipline();
            }
            this.discipline.setName(disciplineName);
            return this;
        }

        public Builder setDisciplinePracticeHours(long practiceHours) {
            if (this.discipline == null) {
                this.discipline = new Discipline();
            }
            this.discipline.setPracticeHours(practiceHours);
            return this;
        }

        public LabWork build() {
            return new LabWork(
                    name,
                    coordinates,
                    minimalPoint,
                    description,
                    difficulty,
                    discipline
            );
        }

        public Builder setCoordinateX(double x) {
            if (this.coordinates == null) {
                this.coordinates = new Coordinates();
            }
            this.coordinates.setX(x);
            return this;
        }

        public Builder setCoordinateY(Float y) {
            if (this.coordinates == null) {
                this.coordinates = new Coordinates();
            }
            this.coordinates.setY(y);
            return this;
        }
    }
}