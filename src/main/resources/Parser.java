import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class Parser/* id */ {

    private static final String END = "$R";

    private static List<Production> productions = new ArrayList<>();
    private static List<Action> actions = new ArrayList<>();
    private static List<GoTo> goTos = new ArrayList<>();

    private static String action(Integer state, String t) {
        for (Action action : actions) {
            if (action.sid == state && action.terminal.equals(t))
                return action.action;
        }
        return "error";
    }

    private static Integer goTo(Integer state, String nonT) {
        for (GoTo goTo : goTos) {
            if(goTo.sid == state && goTo.nonTerminal.equals(nonT))
                return goTo.goTo;
        }
        return -1;
    }

    static {
        /* initialize code */
    }

    public static void main(String[] args) {
        if (args.length == 0)
            System.out.println("请输入输入文件及输出文件地址");
        File input = new File(args[0]);
        File output = new File(args[1]);

        List<String> tokens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(input))){
            String line = null;
            while((line = reader.readLine()) != null) {
                tokens.addAll(new ArrayList<>(Arrays.asList(line.split(" "))));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Production> reduces = new ArrayList<>();
        tokens.add(END);

        Stack<Integer> stateS = new Stack<>();
        Stack<String> symbolS = new Stack<>();
        stateS.push(0);
        symbolS.push(END);
        int reader = 0;
        while (true) {
            String token = tokens.get(reader);
            Integer state = stateS.peek();
            String action = action(state, token);
            if(action.charAt(0) == 's') {
                Integer stateToShift = Integer.parseInt(action.substring(1));
                stateS.push(stateToShift);
                symbolS.push(token);
                reader++;
            } else if(action.charAt(0) == 'r') {
                Integer proUsed = Integer.parseInt(action.substring(1));
                Production pro = productions.get(proUsed);
                reduces.add(pro);
                // 接受
                if (proUsed == 0)
                    break;
                int i = 0;
                while(i < pro.right.size()) {
                    symbolS.pop();
                    stateS.pop();
                    i++;
                }
                symbolS.push(pro.left);
                Integer stateToGo = goTo(stateS.peek(), symbolS.peek());
                stateS.push(stateToGo);
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))){
            for (Production production : reduces) {
                writer.write(production.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
class Production {
    int id;
    String left;
    List<String> right;

    public Production(int id, String pro) {
        this.id = id;
        String[] parts = pro.split("->");
        this.left = parts[0].trim();
        this.right = new ArrayList<>(Arrays.asList(parts[1].trim().split(" ")));
    }

    @Override
    public String toString() {
        String right = this.right.stream().collect(Collectors.joining(" "));
        return this.left + " -> " + right;
    }
}

class Action {
    int sid;
    String terminal;
    String action;

    public Action(int sid, String terminal, String action) {
        this.sid = sid;
        this.terminal = terminal;
        this.action = action;
    }
}

class GoTo {
    int sid;
    String nonTerminal;
    int goTo;

    public GoTo(int sid, String nonTerminal, int goTo) {
        this.sid = sid;
        this.nonTerminal = nonTerminal;
        this.goTo = goTo;
    }
}