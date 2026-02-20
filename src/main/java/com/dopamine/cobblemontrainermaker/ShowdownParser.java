package com.dopamine.cobblemontrainermaker;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.stream.Collectors;


public class ShowdownParser {
    List<Pokemon> team = new ArrayList<>();
    Integer idPokemon;

    public Integer addTeam(Pokemon pokemon) {
        this.team.add(pokemon);
        return team.size();
    }

    public static String[] parseFirstLine(String line) {
    if (line.contains("@")) {
        String[] parts = line.split("@");
        String specie = parts[0].replaceAll("\\(.*?\\)", "").trim();
        String item = parts[1].trim();
        return new String[]{specie, item};
    } else {
        String specie = line.replaceAll("\\(.*?\\)", "").trim();
        return new String[]{specie, ""};
    }
}

    public static String parseAbility(String content){
        return content.replace("Ability: ", "").trim().toLowerCase();
    }

    public static Integer parseLevel(String[] lines){
        for (String line : lines){
            if (line.contains("Level: ")){
                return Integer.parseInt(line.replace("Level: ", "").trim());
            }
        }
        return 100;
    }

    public static Boolean parseShiny(String[] lines){
        for (String line : lines){
            if (line.contains("Shiny: ")){
                return line.replace("Shiny: ", "").trim().equals("Yes");
            }
        }
        return false;
    }

    public static String parseTeraType(String[] lines){
        for (String line : lines){
            if (line.contains("Tera Type: ")){
                return line.replace("Tera Type: ", "").trim().toLowerCase();
            }
        }
        return "normal";
    }

    public static Integer[] parseEvs(String[] lines){
        Integer[] evsArray = new Pokemon().getEvs();
        String[] evs;
        String stat;
        Integer value;
        for (String line : lines){
            if (line.contains("EVs: ")){
                evs = line.trim().replace("EVs: ","").split("/");
                setIvEv(evsArray, evs);
            }
        }
        return  evsArray;
    }

    public static String parseNature(String[] lines){
        for (String line : lines){
            if (line.toLowerCase().contains("nature")){
                return line.trim().split(" ")[0].toLowerCase();
            }
        }
        return "hardy";
    }

    public static Integer[] parseIvs(String[] lines){
        Integer[] ivsArray = new Pokemon().getIvs();
        String[] ivs;
        String stat;
        Integer value;
        for (String line : lines){
            if (line.contains("IVs: ")){
                ivs = line.trim().replace("IVs: ","").split("/");
                setIvEv(ivsArray, ivs);
                break;
            }
        }
        return  ivsArray;
    }

    public static List<String> parseAttacks(String block){
        Pattern pattern = Pattern.compile("^\\s*- (.*)", Pattern.MULTILINE);
        return pattern.matcher(block).results()
                .map(mr -> mr.group(1).trim()).toList();
    }

    private static void setIvEv(Integer[] ivsArray, String[] ivs) {
        Integer value;
        String stat;
        for (String iv : ivs) {
            value = Integer.parseInt(iv.trim().split("\\s+")[0]);
            stat = iv.trim().split("\\s+")[1];
            switch (stat) {
                case "HP":
                    ivsArray[0] = value;
                    break;
                case "Atk":
                    ivsArray[1] = value;
                    break;
                case "Def":
                    ivsArray[2] = value;
                    break;
                case "SpA":
                    ivsArray[3] = value;
                    break;
                case "SpD":
                    ivsArray[4] = value;
                    break;
                case "Spe":
                    ivsArray[5] = value;
            }
        }
    }


    public void parse(String content) {
        String[] pokemonBlocks = splitTeam(content);
        Pokemon pokemon;
        for (String pokemonBlock : pokemonBlocks) {
            String[] lines = pokemonBlock.split("\n");
            pokemon = new Pokemon();

            // Parse first line
            String[]fLine = parseFirstLine(lines[0]);
            pokemon.setSpecie(fLine[0].toLowerCase());
            pokemon.setItem(fLine[1].toLowerCase());

            // Parse Ability
            pokemon.setAbility(parseAbility(lines[1]));

            // Parse Level if exist
            pokemon.setLevel(parseLevel(lines));

            // Parse Shiny if exist
            pokemon.setShiny(parseShiny(lines));

            // Parse Tera Type
            pokemon.setTeraType(parseTeraType(lines));

            // Parse EVs
            pokemon.setEvs(parseEvs(lines));

            // Parse Nature
            pokemon.setNature(parseNature(lines));

            // Parse IVs
            pokemon.setIvs(parseIvs(lines));

            // Parse Attacks
            pokemon.setAttacks(parseAttacks(pokemonBlock));
            if (addTeam(pokemon)>= 6){
                break;
            };
        }
        for (Pokemon pok: team){
            pok.displayPokemon();
            System.out.println("=============");
        }
    }

    public static String[] splitTeam(String content) {
        return content.split("(\\R\\s*){2,}");
    }

    public String getTeam() {
        Map<String, String> pokemonMap = loadJsonMap("/datas/pokemons.json");
        Map<String, String> itemMap = loadJsonMap("/datas/items.json");
        Map<String, String> aspectMap = loadJsonMap("/datas/aspects.json");
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (int i = 0; i < team.size(); i++) {
            sb.append(pokemonToJson(team.get(i), pokemonMap, itemMap, aspectMap));
            if (i < team.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("]");
        return sb.toString();
    }

    private static Map<String, String> loadJsonMap(String resourcePath) {
        Map<String, String> map = new LinkedHashMap<>();
        InputStream is = ShowdownParser.class.getResourceAsStream(resourcePath);
        if (is == null) {
            System.err.println("Resource not found: " + resourcePath);
            return map;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String content = reader.lines().collect(Collectors.joining("\n"));
            Pattern p = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");
            Matcher m = p.matcher(content);
            while (m.find()) {
                map.put(m.group(1), m.group(2));
            }
        } catch (Exception e) {
            System.err.println("Failed to load " + resourcePath + ": " + e.getMessage());
        }
        return map;
    }

    private static String pokemonToJson(Pokemon p, Map<String, String> pokemonMap, Map<String, String> itemMap, Map<String, String> aspectMap) {
        String species = resolveSpecies(p.getSpecie(), pokemonMap);
        String heldItem = resolveItem(p.getItem(), itemMap);
        String aspect = aspectMap.get(p.getSpecie());
        StringBuilder sb = new StringBuilder();
        sb.append("  {\n");
        sb.append("    \"species\": \"").append(species).append("\",\n");
        if (aspect != null) {
            sb.append("    \"aspects\": [\"").append(aspect).append("\"],\n");
        }
        sb.append("    \"level\": ").append(p.getLevel()).append(",\n");
        sb.append("    \"nature\": \"cobblemon:").append(p.getNature()).append("\",\n");
        sb.append("    \"ability\": \"").append(p.getAbility()).append("\",\n");
        sb.append("    \"shiny\": ").append(p.getShiny());
        if (!heldItem.isEmpty()) {
            sb.append(",\n    \"heldItem\": \"").append(heldItem).append("\"");
        }
        if (!p.getAttacks().isEmpty()) {
            sb.append(",\n    \"moveset\": ").append(buildMoveset(p.getAttacks()));
        }
        sb.append(",\n    \"ivs\": ").append(buildStats(p.getIvs()));
        sb.append(",\n    \"evs\": ").append(buildStats(p.getEvs()));
        sb.append("\n  }");
        return sb.toString();
    }

    private static String resolveSpecies(String showdownName, Map<String, String> pokemonMap) {
        if (pokemonMap.containsKey(showdownName)) {
            return pokemonMap.get(showdownName);
        }
        return "cobblemon:" + showdownName.replace(" ", "_");
    }

    private static String resolveItem(String showdownItem, Map<String, String> itemMap) {
        if (showdownItem.isEmpty()) return "";
        if (itemMap.containsKey(showdownItem)) {
            return itemMap.get(showdownItem);
        }
        return "cobblemon:" + showdownItem.replace(" ", "_").replace("-", "_");
    }

    private static String buildMoveset(List<String> attacks) {
        StringBuilder sb = new StringBuilder("[\n");
        for (int i = 0; i < attacks.size(); i++) {
            String move = attacks.get(i).toLowerCase().replace(" ", "").replace("-", "");
            sb.append("      \"").append(move).append("\"");
            if (i < attacks.size() - 1) sb.append(",");
            sb.append("\n");
        }
        sb.append("    ]");
        return sb.toString();
    }

    private static String buildStats(Integer[] stats) {
        return String.format("{ \"hp\": %d, \"atk\": %d, \"def\": %d, \"spa\": %d, \"spd\": %d, \"spe\": %d }",
                stats[0], stats[1], stats[2], stats[3], stats[4], stats[5]);
    }

    public static void main(String[] args) {
        String contentSingle = """
                        Sandy Shocks @ Assault Vest \s
                            Ability: Protosynthesis \s
                            Level: 99 \s
                            Tera Type: Rock \s
                            EVs: 140 HP / 4 Atk / 108 SpD / 44 Spe \s
                            Quirky Nature \s
                            IVs: 0 Spe \s
                            - Body Slam \s
                            - Iron Defense \s
                            - Facade
            """;
        String contentTeam = """
                        Kingdra @ Choice Specs  
                        Ability: Swift Swim  
                        Shiny: Yes  
                        Tera Type: Poison  
                        EVs: 252 SpA / 4 SpD / 252 Spe  
                        Timid Nature  
                        IVs: 0 Atk  
                        - Draco Meteor  
                        - Dragon Pulse  
                        - Surf  
                        - Hurricane  
                        
                        Swampert (M) @ Leftovers  
                        Ability: Torrent  
                        Level: 55  
                        Tera Type: Water  
                        EVs: 248 HP / 248 Atk / 8 SpD  
                        Adamant Nature  
                        IVs: 0 Atk / 20 SpA / 5 Spe  
                        - Earthquake  
                        - Flip Turn  
                        - Stealth Rock  
                        - Knock Off  
                        
                        Pelipper @ Heavy-Duty Boots  
                        Ability: Drizzle  
                        Tera Type: Water  
                        EVs: 248 HP / 252 Def / 8 SpD  
                        Relaxed Nature  
                        IVs: 20 Spe  
                        - Hurricane  
                        - Ice Beam  
                        - U-turn  
                        - Roost  
                        
                        Primarina @ Assault Vest  
                        Ability: Torrent  
                        Tera Type: Water  
                        EVs: 80 HP / 252 SpA / 168 SpD / 8 Spe  
                        Modest Nature  
                        - Surf  
                        - Moonblast  
                        - Psychic Noise  
                        - Flip Turn  
                        
                        Barraskewda  
                        Ability: Swift Swim  
                        Tera Type: Water  
                        
                        Rotom-Wash  
                        Ability: Levitate  
                        Tera Type: Electric  
                    """;
        ShowdownParser parser = new ShowdownParser();
        parser.parse(contentTeam);
        System.out.println(parser.getTeam());
    }
}
