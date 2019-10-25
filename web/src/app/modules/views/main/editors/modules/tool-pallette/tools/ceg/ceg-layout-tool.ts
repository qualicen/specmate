import { ToolBase } from '../tool-base';
import { mxgraph } from 'mxgraph';
import { Config } from '../../../../../../../../config/config';


class Dimension {
    constructor(public width: number, public height: number) {}
}

export class CEGLayoutTool extends ToolBase {
    public isVertexTool = false;
    public color = 'primary';
    public icon = 'sitemap';
    public name = 'tools.layout';
    public style = '';

    public perform(): Promise<any> {
        return this.layoutGraph();
    }

    public layoutGraph(): Promise<any> {
        let nodeOrdering = this.toposort(this.graph);
        let dimTable = this.getDimensions(nodeOrdering);
        this.graph.model.beginUpdate();
        try {
            this.updateLayout(nodeOrdering, dimTable);
        } finally {
            this.graph.model.endUpdate();
        }
        return Promise.resolve();
    }

    private toposort(graph: mxgraph.mxGraph): mxgraph.mxCell[][] {
        const positionTable: {[key: string]: number} = {};
        const parentCount: {[key: string]: number} = {};
        const nodeList = graph.getModel().getChildVertices(graph.getDefaultParent()).filter(n => !n.isEdge());
        const workList: mxgraph.mxCell[] = [];

        let maxPosition = 0;
        for (const node of nodeList) {
            parentCount[node.getId()] = 0;
            if (node.edges !== undefined && node.edges !== null) {
                for (const edge of node.edges) {
                    if (edge.source == node) {
                        parentCount[node.getId()]++;
                    }
                }
            }
            if (parentCount[node.getId()] == 0) {
                positionTable[node.getId()] = 0;
                workList.push(node);
            }

        }

        while (workList.length > 0) {
            let current = workList.pop();
            if (current.edges === undefined || current.edges === null) {
                continue;
            }
            for (const edge of current.edges) {
                if (edge.target == current) {
                    parentCount[edge.source.getId()]--;
                    if (parentCount[edge.source.getId()] == 0) {
                        let position = positionTable[current.getId()] + 1;
                        if (maxPosition < position) {
                            maxPosition = position;
                        }
                        positionTable[edge.source.getId()] = position;
                        workList.push(edge.source);
                    }
                }
            }
        }

        let nodeOrdering: mxgraph.mxCell[][] = new Array(maxPosition + 1);
        for (const node of nodeList) {
            if (positionTable[node.getId()] === undefined) {
                // We have a cycle in the graph...
                // If the user can't be asked to build a proper tree, we won't layout that
                return [];
            }

            let position = maxPosition - positionTable[node.getId()];
            if (nodeOrdering[position] !== undefined && nodeOrdering[position] !== null) {
                nodeOrdering[position].push(node);
            } else {
                nodeOrdering[position] = [node];
            }
        }
        return nodeOrdering;
    }

    private getDimensions(nodeOrdering: mxgraph.mxCell[][]): Dimension[] {
        let dimensions: Dimension[] = [];
        for (const layer of nodeOrdering) {
            let width = 0;
            let height = 0;
            for (const cell of layer) {
                if (cell.geometry.width > width) {
                    width = cell.getGeometry().width;
                }
                height += cell.getGeometry().height;
            }
            height += (layer.length - 1) * Config.CEG_LAYOUT_CLEARANCE_Y;
            dimensions.push(new Dimension(width, height));
        }
        return dimensions;
    }

    private updateLayout(nodeOrdering: mxgraph.mxCell[][], dimTable: Dimension[]) {
        let layerPositionX = Config.CEG_LAYOUT_CLEARANCE_X;
        let layerPositionY = Config.CEG_LAYOUT_CLEARANCE_Y;
        let maxHight = Math.max(...dimTable.map(d => d.height));

        for (let layerIndex = 0; layerIndex < dimTable.length; layerIndex++) {
            const layerNodes = nodeOrdering[layerIndex];
            const layerDimensions = dimTable[layerIndex];
            let dynamicYOffset = (maxHight - layerDimensions.height) / (layerNodes.length + 1);
            let yOffset = dynamicYOffset;
            for (let nodeIndex = 0; nodeIndex < layerNodes.length; nodeIndex++) {
                const node = layerNodes[nodeIndex];
                let nodeClearanceX = 0.5 * (layerDimensions.width - node.getGeometry().width);
                let nodeX = layerPositionX + nodeClearanceX;
                let nodeY = layerPositionY + yOffset;
                yOffset += node.getGeometry().height + Config.CEG_LAYOUT_CLEARANCE_Y + dynamicYOffset;
                let geometry = node.getGeometry().clone() as mxgraph.mxGeometry;
                geometry.x = nodeX;
                geometry.y = nodeY;
                this.graph.getModel().setGeometry(node, geometry);
            }
            layerPositionX += Config.CEG_LAYOUT_CLEARANCE_X + layerDimensions.width;
        }
    }
}
