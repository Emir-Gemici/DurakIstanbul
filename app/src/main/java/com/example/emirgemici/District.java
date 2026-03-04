package com.example.emirgemici;

/**
 * Sahibem, bu sınıf sadece ilçe verilerini taşır.
 */
public class District {
    private String name;
    private String side;
    private boolean isSelected;

    public District(String name, String side) {
        this.name = name;
        this.side = side;
        this.isSelected = false;
    }

    public String getName() { return name; }
    public String getSide() { return side; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
}