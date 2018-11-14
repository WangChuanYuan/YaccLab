import cfg.CFG;
import cfg.Production;
import table.Action;
import table.GoTo;
import table.LRParsingTable;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        String cfgId = "CFG1";
        File file = new File(Main.class.getResource(cfgId + ".y").getFile());
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
//        String input = "i + i * i + ( i + i )";
//        List<String> tokens = new ArrayList<>(Arrays.asList(input.split(" ")));
//        List<Production> reduces = table.parse(tokens);
//        for(Production production : reduces)
//            System.out.println(production.toString());
        File template = new File(Main.class.getResource("Parser.java").getFile());
        String content = "";
        try (BufferedReader reader = new BufferedReader(new FileReader(template))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                content += line;
                content += System.lineSeparator();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        content = content.replace("/* id */", cfgId);
        StringBuilder initialCode = new StringBuilder();
        for (Production production : table.getProductions()) {
            String code = "productions.add(new Production(" + production.getId() + ",\"" + production.toString() + "\"));\n";
            initialCode.append(code);
        }
        for (Action action : table.getActions()) {
            String code = "actions.add(new Action(" + action.getSid() + ",\"" + action.getTerminal() + "\",\"" + action.getAction() + "\"));\n";
            initialCode.append(code);
        }
        for (GoTo goTo : table.getGoTos()) {
            String code = "goTos.add(new GoTo(" + goTo.getSid() + ",\"" + goTo.getNonTerminal() + "\"," + goTo.getGoTo() + "));\n";
            initialCode.append(code);
        }
        content = content.replace("/* initialize code */", initialCode);
        File parser = new File(Main.class.getResource("").getPath() + File.separator + "Parser" + cfgId +".java");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(parser))){
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
