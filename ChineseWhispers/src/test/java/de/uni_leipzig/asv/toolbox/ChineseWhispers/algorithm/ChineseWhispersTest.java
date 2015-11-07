package de.uni_leipzig.asv.toolbox.ChineseWhispers.algorithm;

import org.junit.Test;

public class ChineseWhispersTest {

    @Test
    public void test() {

        ChineseWhispers cw = new ChineseWhispers();
        cw.isNumbered = false;
        cw.graphInMemory = true;

        String nodesFile = "E:/workspace/test/nodes/D0739I.node";
        String edgesFile = "E:/workspace/test/edges/int-val/D0739I.edge";
        String outFile = "E:/workspace/test/cwResult";
        cw.setCWGraph(nodesFile, edgesFile);
        cw.setCWParameters(200, "dist", "nolog", 0.0, "dec", 0.0, "continuous", 20, true);
        cw.run();

        cw.consoleChooserString_colors = outFile;
        cw.consoleChooserString_colors_read = outFile + ".read";
        cw.writeFile(false, true, true);

    }

}
