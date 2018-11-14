package cfg;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Production {

    private int id;

    private String left;

    private List<String> right;

    @Override
    public String toString() {
        String right = this.right.stream().collect(Collectors.joining(" "));
        return this.left + " -> " + right;
    }
}
