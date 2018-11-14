package table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LRItem implements Comparable<LRItem> {

    /**
     * 产生式编号
     */
    private int pid;

    /**
     * 点的位置
     * 在第i个字符前
     */
    private int dotPos;

    /**
     * 预测符
     */
    private String predict;

    @Override
    public boolean equals(Object o) {
        if (o instanceof LRItem) {
            LRItem item = (LRItem) o;
            return this.pid == item.pid && this.dotPos == item.dotPos && this.predict.equals(item.predict);
        }
        return super.equals(o);
    }

    @Override
    public int compareTo(LRItem o) {
        if (this.pid < o.pid)
            return -1;
        else if (this.pid > o.pid)
            return 1;
        else {
            if (this.dotPos < o.dotPos)
                return -1;
            else if (this.dotPos > o.dotPos)
                return 1;
            else return this.predict.compareTo(o.predict);
        }
    }
}
