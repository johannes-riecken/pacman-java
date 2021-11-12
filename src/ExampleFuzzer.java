import com.company.*;
import java.util.*;

public class ExampleFuzzer {
    public static void fuzzerTestOneInput(byte[] input) {
        if (input.length < 2) {
            return;
        }
        byte x = input[0];
        byte y = input[1];
        if (input.length < 2 + x * y) {
            return;
        }
        var m = new ArrayList<List<Boolean>>();
        for (var i = 0; i < x; i++) {
            m.add(new ArrayList<>());
            for (var j = 0; j < y; j++) {
                m.get(m.size() - 1).add(input[i * j + 2] < 0);
            }
        }
        Maze.getStringFromMaze(m);
    }
}
