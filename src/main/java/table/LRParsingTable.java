package table;

import cfg.Production;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import util.Const;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LRParsingTable {

    private List<Production> productions;

    private List<Action> actions;

    private List<GoTo> goTos;

    private String action(Integer state, String t) {
        for (Action action : actions) {
            if (action.getSid() == state && action.getTerminal().equals(t))
                return action.getAction();
        }
        return "error";
    }

    private Integer goTo(Integer state, String nonT) {
        for (GoTo goTo : goTos) {
            if(goTo.getSid() == state && goTo.getNonTerminal().equals(nonT))
                return goTo.getGoTo();
        }
        return -1;
    }

    /**
     *
     * @param tokens
     * @return 规约序列
     */
    public List<Production> parse(List<String> tokens) {
        List<Production> reduces = new ArrayList<>();
        // 添加结束符
        tokens.add(Const.END);

        Stack<Integer> stateS = new Stack<>(); // 状态栈
        Stack<String> symbolS = new Stack<>(); // 符号栈
        stateS.push(0);
        symbolS.push(Const.END);
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
                while(i < pro.getRight().size()) {
                    symbolS.pop();
                    stateS.pop();
                    i++;
                }
                symbolS.push(pro.getLeft());
                Integer stateToGo = goTo(stateS.peek(), symbolS.peek());
                stateS.push(stateToGo);
            }
        }
        return reduces;
    }
}
