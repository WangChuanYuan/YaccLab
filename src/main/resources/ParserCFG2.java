import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class ParserCFG2 {

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
        productions.add(new Production(0,"E' -> E"));
productions.add(new Production(1,"E -> E + T"));
productions.add(new Production(2,"E -> T"));
productions.add(new Production(3,"T -> T * F"));
productions.add(new Production(4,"T -> F"));
productions.add(new Production(5,"F -> ( E )"));
productions.add(new Production(6,"F -> i"));
actions.add(new Action(0,"(","s4"));
actions.add(new Action(0,"i","s5"));
actions.add(new Action(1,"$R","r2"));
actions.add(new Action(1,"+","r2"));
actions.add(new Action(1,"*","s6"));
actions.add(new Action(2,"$R","r0"));
actions.add(new Action(2,"+","s7"));
actions.add(new Action(3,"$R","r4"));
actions.add(new Action(3,"+","r4"));
actions.add(new Action(3,"*","r4"));
actions.add(new Action(4,"(","s11"));
actions.add(new Action(4,"i","s12"));
actions.add(new Action(5,"$R","r6"));
actions.add(new Action(5,"+","r6"));
actions.add(new Action(5,"*","r6"));
actions.add(new Action(6,"(","s4"));
actions.add(new Action(6,"i","s5"));
actions.add(new Action(7,"(","s4"));
actions.add(new Action(7,"i","s5"));
actions.add(new Action(8,")","r2"));
actions.add(new Action(8,"+","r2"));
actions.add(new Action(8,"*","s15"));
actions.add(new Action(9,")","s16"));
actions.add(new Action(9,"+","s17"));
actions.add(new Action(10,")","r4"));
actions.add(new Action(10,"+","r4"));
actions.add(new Action(10,"*","r4"));
actions.add(new Action(11,"(","s11"));
actions.add(new Action(11,"i","s12"));
actions.add(new Action(12,")","r6"));
actions.add(new Action(12,"+","r6"));
actions.add(new Action(12,"*","r6"));
actions.add(new Action(13,"$R","r3"));
actions.add(new Action(13,"+","r3"));
actions.add(new Action(13,"*","r3"));
actions.add(new Action(14,"$R","r1"));
actions.add(new Action(14,"+","r1"));
actions.add(new Action(14,"*","s6"));
actions.add(new Action(15,"(","s11"));
actions.add(new Action(15,"i","s12"));
actions.add(new Action(16,"$R","r5"));
actions.add(new Action(16,"+","r5"));
actions.add(new Action(16,"*","r5"));
actions.add(new Action(17,"(","s11"));
actions.add(new Action(17,"i","s12"));
actions.add(new Action(18,")","s21"));
actions.add(new Action(18,"+","s17"));
actions.add(new Action(19,")","r3"));
actions.add(new Action(19,"+","r3"));
actions.add(new Action(19,"*","r3"));
actions.add(new Action(20,")","r1"));
actions.add(new Action(20,"+","r1"));
actions.add(new Action(20,"*","s15"));
actions.add(new Action(21,")","r5"));
actions.add(new Action(21,"+","r5"));
actions.add(new Action(21,"*","r5"));
goTos.add(new GoTo(0,"T",1));
goTos.add(new GoTo(0,"E",2));
goTos.add(new GoTo(0,"F",3));
goTos.add(new GoTo(4,"T",8));
goTos.add(new GoTo(4,"E",9));
goTos.add(new GoTo(4,"F",10));
goTos.add(new GoTo(6,"F",13));
goTos.add(new GoTo(7,"T",14));
goTos.add(new GoTo(7,"F",3));
goTos.add(new GoTo(11,"T",8));
goTos.add(new GoTo(11,"E",18));
goTos.add(new GoTo(11,"F",10));
goTos.add(new GoTo(15,"F",19));
goTos.add(new GoTo(17,"T",20));
goTos.add(new GoTo(17,"F",10));

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
