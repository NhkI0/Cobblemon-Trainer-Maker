package com.dopamine.cobblemontrainermaker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Pokemon {
    String specie = "";
    String item;
    String ability;
    Integer level;
    Boolean shiny;
    String teraType;
    final String[] teraTypes = {
            "normal", "fighting", "flying", "poison",
            "ground", "rock", "bug", "ghost",
            "steel", "fire", "water", "grass",
            "electric", "psychic", "ice", "dragon",
            "dark", "fairy", "stellar"
    };
    Integer[] evs;
    String nature;
    final String[] natures = {"hardy", "lonely", "brave", "adamant", "naughty", "bold",
                        "docile", "relaxed", "impish", "lax", "timid", "hasty",
                        "serious", "jolly", "naïve", "modest", "mild", "quiet",
                        "bashful", "rash", "calm", "gentle", "sassy", "careful",
                        "quirky"};
    Integer[] ivs;
    List<String> Attacks;

    public Pokemon() {
        this.specie = "";
        this.item = "";
        this.ability = "";
        this.level = 1;
        this.shiny = false;
        this.teraType = "normal";
        this.evs = new Integer[]{
            0, // HP
            0, // Attack
            0, // Def
            0, // Spe Attack
            0, // Spe Def
            0  // Speed
        };
        this.nature = "hardy";
        this.ivs = new Integer[] {
            31, // HP
            31, // Attack
            31, // Def
            31, // Spe Attack
            31, // Spe Def
            31  // Speed
        };
        this.Attacks = new ArrayList<String>();
    }

    public void displayPokemon() {
        System.out.println("Pokemon:");
        System.out.println("Specie: " + specie);
        System.out.println("Item: " + item);
        System.out.println("Ability: " + ability);
        System.out.println("Level: " + level);
        System.out.println("Shiny: " + shiny);
        System.out.println("TeraType: " + teraType);
        System.out.println("Evs: " + Arrays.toString(evs));
        System.out.println("Nature: " + nature);
        System.out.println("Ivs: " + Arrays.toString(ivs));
        System.out.println("Attacks: " + Arrays.toString(Attacks.toArray()));
    }

    public String getSpecie() {
        return specie;
    }

    public void setSpecie(String specie) {
        this.specie = specie;
    }

    public String getItem() {
        return item;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getAbility() {
        return ability;
    }

    public void setAbility(String ability) {
        this.ability = ability;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Boolean getShiny() {
        return shiny;
    }

    public void setShiny(Boolean shiny) {
        this.shiny = shiny;
    }

    public String getTeraType() {
        return teraType;
    }

    public void setTeraType(String teraType) {
        if (Arrays.asList(teraTypes).contains(teraType)) {
            this.teraType = teraType;
        }
    }

    public String[] getTeraTypes() {
        return teraTypes;
    }

    public Integer[] getEvs() {
        return evs;
    }

    public void setEvs(Integer[] evs) {
        this.evs = evs;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        if (Arrays.asList(natures).contains(nature)) {
            this.nature = nature;
        }
    }

    public String[] getNatures() {
        return natures;
    }

    public Integer[] getIvs() {
        return ivs;
    }

    public void setIvs(Integer[] ivs) {
        this.ivs = ivs;
    }

    public List<String> getAttacks() {
        return Attacks;
    }

    public void setAttacks(List<String> attacks) {
        Attacks = attacks;
    }
}
