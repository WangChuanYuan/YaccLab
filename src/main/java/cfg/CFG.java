package cfg;

import lombok.Data;
import lombok.NoArgsConstructor;
import table.*;
import util.Const;

import java.util.*;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class CFG {

    private String start;

    private List<Production> productions;

    private List<String> nonTerminals;

    private List<String> terminals;

    private Map<String, List<String>> first;

    public CFG(String start, List<String> productions, List<String> nonTerminals, List<String> terminals) {
        // 添加0号产生式
        this.start = start + "'";
        Production zero = new Production(0, this.start, new ArrayList<>(Arrays.asList(start)));
        this.productions = new ArrayList<>();
        this.productions.add(zero);
        // 添加其他产生式
        for (int i = 0; i < productions.size(); i++) {
            String[] parts = productions.get(i).split("->");
            String left = parts[0].trim();
            List<String> right = new ArrayList<>(Arrays.asList(parts[1].trim().split(" ")));
            // 去除EPSILON
            right.removeAll(Collections.singleton(Const.EPSILON));
            this.productions.add(new Production(i + 1, left, right));
        }
        this.nonTerminals = nonTerminals;
        this.terminals = terminals;
        // 计算first
        this.first = new HashMap<>();
        while (true) {
            int lastSize = this.first.values().stream().mapToInt(List::size).sum();
            for (String nonT : nonTerminals) {
                for (Production pro : this.productions) {
                    if (!pro.getLeft().equals(nonT))
                        continue;
                    for (int i = 0; i < pro.getRight().size(); i++) {
                        String symbol = pro.getRight().get(i);
                        if (!nonT.equals(symbol)) {
                            if (nonTerminals.contains(symbol)) {
                                List<String> sFirst = first.get(symbol) == null ? new ArrayList<>() : first.get(symbol);
                                if (this.first.get(nonT) == null) {
                                    this.first.put(nonT, sFirst);
                                } else {
                                    for (String str : sFirst)
                                        if (!this.first.get(nonT).contains(str))
                                            this.first.get(nonT).add(str);
                                }
                                break;
                            } else {
                                if (this.first.get(nonT) == null) {
                                    List<String> sFirst = new ArrayList<>(Arrays.asList(symbol));
                                    this.first.put(nonT, sFirst);
                                } else if (!this.first.get(nonT).contains(symbol)) {
                                    this.first.get(nonT).add(symbol);
                                }
                                break;
                            }
                        }
                    }
                }
            }
            if (this.first.values().stream().mapToInt(List::size).sum() == lastSize)
                break;
        }
    }

    private boolean isKernelsEqual(List<LRItem> kernels1, List<LRItem> kernels2) {
        if (kernels1.size() != kernels2.size())
            return false;
        kernels1 = kernels1.stream().sorted().collect(Collectors.toList());
        kernels2 = kernels2.stream().sorted().collect(Collectors.toList());
        for (int i = 0; i < kernels1.size(); i++) {
            if (!kernels1.get(i).equals(kernels2.get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param beta beta = X1 X2 X3 ...
     * @return
     */
    private List<String> first(List<String> beta) {
        List<String> first = new ArrayList<>();
        if (nonTerminals.contains(beta.get(0))) {
            for (String symbol : this.first.get(beta.get(0))) {
                if (!first.contains(symbol))
                    first.add(symbol);
            }
        } else {
            if (!first.contains(beta.get(0)))
                first.add(beta.get(0));
        }
        return first;
    }

    private void inStateExtension(State state) {
        List<LRItem> items = state.getItems();
        for (int i = 0; i < items.size(); i++) {
            LRItem item = items.get(i);
            // 非规约项
            Production production = productions.get(item.getPid());
            if (item.getDotPos() < production.getRight().size()) {
                String nextSymbol = production.getRight().get(item.getDotPos());
                if (nonTerminals.contains(nextSymbol)) {
                    List<String> beta = (item.getDotPos() == production.getRight().size() - 1) ? new ArrayList<>()
                            : new ArrayList<>(production.getRight().subList(item.getDotPos() + 1, production.getRight().size()));
                    beta.add(item.getPredict());
                    for (Production pro : productions) {
                        if (pro.getLeft().equals(nextSymbol)) {
                            List<String> predicts = first(beta);
                            for (String predict : predicts) {
                                LRItem newItem = new LRItem(pro.getId(), 0, predict);
                                if (!items.contains(newItem))
                                    items.add(newItem);
                            }
                        }
                    }
                }
            }
        }
    }

    public LRParsingTable LRParsingTable() {
        int sid = 0; // 状态编号
        int kid = 0; // 状态核编号，与状态编号保持一致
        // LR ParsingTable
        LRParsingTable table = new LRParsingTable(productions, new ArrayList<>(), new ArrayList<>());
        // 初始项
        LRItem zeroItem = new LRItem(0, 0, Const.END);
        List<LRItem> zeroKernel = new ArrayList<>(Arrays.asList(zeroItem));
        // 状态集
        List<State> states = new ArrayList<>();
        states.add(new State(sid++, zeroKernel));
        // 状态核集 (用于辨识是否是新的状态)，数量应该与状态集保持一致
        List<State> kernels = new ArrayList<>();
        kernels.add(new State(kid++, zeroKernel));
        // 计算状态集
        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            // 状态内扩展
            inStateExtension(state);
            // 状态间扩展以及生成分析表(Reduction)
            Map<String, List<LRItem>> shifts = new HashMap<>();
            for (LRItem item : state.getItems()) {
                Production production = productions.get(item.getPid());
                if (item.getDotPos() < production.getRight().size()) {
                    String next = production.getRight().get(item.getDotPos());
                    LRItem shiftTo = new LRItem(item.getPid(), item.getDotPos() + 1, item.getPredict());
                    if (shifts.get(next) == null) {
                        List<LRItem> moves = new ArrayList<>(Arrays.asList(shiftTo));
                        shifts.put(next, moves);
                    } else {
                        if (!shifts.get(next).contains(shiftTo))
                            shifts.get(next).add(shiftTo);
                    }
                } else {
                    table.getActions().add(new Action(state.getId(), item.getPredict(), "r" + item.getPid()));
                }
            }
            // 增加新的状态以及生成分析表(Shift 和 table.GoTo)
            Iterator<Map.Entry<String, List<LRItem>>> entries = shifts.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<String, List<LRItem>> entry = entries.next();
                boolean isNewState = true;
                for (State kernel : kernels) {
                    // 状态集核已存在，代表该状态已存在
                    if (isKernelsEqual(kernel.getItems(), entry.getValue())) {
                        if (nonTerminals.contains(entry.getKey()))
                            table.getGoTos().add(new GoTo(state.getId(), entry.getKey(), kernel.getId()));
                        else table.getActions().add(new Action(state.getId(), entry.getKey(), "s" + kernel.getId()));
                        isNewState = false;
                        break;
                    }
                }
                if (isNewState) {
                    int nKid = kid++;
                    int nSid = sid++;
                    // 注意不要用引用。。。很坑
                    kernels.add(new State(nKid, new ArrayList<>(entry.getValue())));
                    states.add(new State(nSid, new ArrayList<>(entry.getValue())));
                    if (nonTerminals.contains(entry.getKey()))
                        table.getGoTos().add(new GoTo(state.getId(), entry.getKey(), nSid));
                    else table.getActions().add(new Action(state.getId(), entry.getKey(), "s" + nSid));
                }
            }
        }
        return table;
    }

}
