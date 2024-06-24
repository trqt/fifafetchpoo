package org.example;
public record Player(String name, String nationality, String club, String id) {
    @Override
    public String toString() {
        return String.format("Name: %s, Nationality: %s, Club: %s", name, nationality, club);
    }
}