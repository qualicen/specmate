import { ToolBase } from '../tool-base';
import { mxgraph } from 'mxgraph';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
  mxBasePath: 'mxgraph'
});

export class ProcessLayoutTool extends ToolBase {
    public isVertexTool: boolean = undefined;
    public color = 'primary';
    public icon = 'sitemap';
    public name = 'tools.autoLayout';
    public style = '';
    public isHidden = false;

    public graph: mxgraph.mxGraph;

    public async perform(): Promise<any> {
        this.layoutGraph();
    }

    public layoutGraph(): void {
        if (this.graph === undefined) {
            return;
        }
        const treeLayout = new mx.mxCompactTreeLayout(this.graph, true, false);
        treeLayout['moveTree'] = true;
        treeLayout['groupPadding'] = 30;
        treeLayout['edgeRouting'] = false;
        treeLayout.execute(this.graph.getDefaultParent());
    }
}
