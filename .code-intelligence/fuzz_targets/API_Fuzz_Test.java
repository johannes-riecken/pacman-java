
package fuzz_targets;

import com.code_intelligence.jazzer.api.FuzzedDataProvider;
import com.company.*;
import java.util.*;

public class API_Fuzz_Test {
	public static void fuzzerTestOneInput(FuzzedDataProvider data) {
		var x = data.consumeInt() % 128;
        var y = data.consumeInt() % 128;
        var m = new ArrayList<List<Boolean>>();
        for (var i = 0; i < x; i++) {
            m.add(new ArrayList<>());
            for (var j = 0; j < y; j++) {
                m.get(m.size() - 1).add(data.consumeBoolean());
            }
        }
        Maze.getStringFromMaze(m);

	}
}
