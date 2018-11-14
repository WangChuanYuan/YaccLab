import cfg.CFG;
import cfg.Production;
import table.LRParsingTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        File file = new File(Main.class.getResource("CFG.y").getFile());
        String start = null;
        List<String> nonTerminals = new ArrayList<>();
        List<String> terminals = new ArrayList<>();
        List<String> productions = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            int state = 0;
            while ((line = reader.readLine()) != null) {
                if (line.equals("%%")) {
                    state++;
                    continue;
                }
                switch (state) {
                    case 0:
                        start = line;
                        break;
                    case 1:
                        nonTerminals.add(line);
                        break;
                    case 2:
                        terminals.add(line);
                        break;
                    case 3:
                        productions.add(line);
                        break;
                }
            }
        } catch (IOException ignored) {
        }
        CFG cfg = new CFG(start, productions, nonTerminals, terminals);
        LRParsingTable table = cfg.LRParsingTable();
        String input = "b d a";
        List<String> tokens = new ArrayList<>(Arrays.asList(input.split(" ")));
        List<Production> reduces = table.parse(tokens);
        for(Production production : reduces)
            System.out.println(production.toString());
    }
}
