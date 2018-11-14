package table;

import cfg.Production;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LRParsingTable {

    private List<Production> productions;

    private List<Action> actions;

    private List<GoTo> goTos;
}
