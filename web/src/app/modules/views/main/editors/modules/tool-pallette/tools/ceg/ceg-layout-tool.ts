import { ToolBase } from '../tool-base';
import { mxgraph } from 'mxgraph';
import { Config } from '../../../../../../../../config/config';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { SelectedElementService } from 'src/app/modules/views/side/modules/selected-element/services/selected-element.service';
import { IContainer } from 'src/app/model/IContainer';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';
import { TranslateService } from '@ngx-translate/core';


class Dimension {
    constructor(public width: number, public height: number) {}
}

export class CEGLayoutTool extends ToolBase {
    public isVertexTool: boolean = undefined;
    public color = 'primary';
    public icon = 'sitemap';
    public name = 'tools.autoLayout';
    public style = '';
    public isHidden = false;

    constructor(dataService: SpecmateDataService, selectedElementService: SelectedElementService,
            parent: IContainer, private modalService: ConfirmationModal, private translate: TranslateService) {
        super(dataService, selectedElementService, parent);
    }

    public perform(): Promise<any> {
        return this.layoutGraph();
    }

    public layoutGraph(): Promise<any> {
        const nodeList = this.graph.getModel().getChildVertices(this.graph.getDefaultParent()).filter(n => !n.isEdge());
        let nodeOrdering = this.toposort(this.graph, nodeList);
        if (nodeOrdering.length == 0 && nodeList.length > 0) {
            return this.modalService.openOk('Error', this.translate.instant('layoutErrorCircle'));
        }
        let dimTable = this.getDimensions(nodeOrdering);
        this.graph.model.beginUpdate();
        try {
            this.updateLayout(nodeOrdering, dimTable);
        } finally {
            this.graph.model.endUpdate();
        }
        return Promise.resolve();
    }

    private toposort(graph: mxgraph.mxGraph, nodeList: mxgraph.mxCell[]): mxgraph.mxCell[][] {
        const positionTable: {[key: string]: number} = {};
        const parentCount: {[key: string]: number} = {};
        const childCount: {[key: string]: number} = {};
        const phaseAWorkList: mxgraph.mxCell[] = [];
        const phaseBWorkList: mxgraph.mxCell[] = [];

        // Find all Causes & Effects
        let maxPosition = 0;
        for (const node of nodeList) {
            parentCount[node.getId()] = 0;
            childCount[node.getId()] = 0;
            if (node.edges !== undefined && node.edges !== null) {
                for (const edge of node.edges) {
                    if (edge.source == node) {
                        parentCount[node.getId()]++;
                    } else {
                        childCount[node.getId()]++;
                    }
                }
            }
            if (parentCount[node.getId()] == 0) {
                positionTable[node.getId()] = 0;
                phaseAWorkList.push(node);
            }
            if (childCount[node.getId()] == 0) {
                phaseBWorkList.push(node);
            }

        }

        // Phase A: Iterate Back to Front to find the rightmost position
        // each node can be
        while (phaseAWorkList.length > 0) {
            let current = phaseAWorkList.pop();
            if (current.edges === undefined || current.edges === null) {
                continue;
            }
            let newPosition = positionTable[current.getId()] + 1;

            for (const edge of current.edges) {
                if (edge.target == current) {
                    if (maxPosition < newPosition) {
                        maxPosition = newPosition;
                    }
                    if (positionTable[edge.source.getId()] === undefined || newPosition > positionTable[edge.source.getId()]) {
                        positionTable[edge.source.getId()] = newPosition;
                    }
                    parentCount[edge.source.getId()]--;
                    if (parentCount[edge.source.getId()] == 0) {
                        phaseAWorkList.push(edge.source);
                    }
                }
            }
        }

        // Phase B: Iterate Front to Back to find the leftmost position each node can be in
        while (phaseBWorkList.length > 0) {
            let current = phaseBWorkList.pop();
            if (current.edges === undefined || current.edges === null) {
                continue;
            }
            if (positionTable[current.getId()] === undefined) {
                // We have a cycle in the graph...
                // If the user can't be asked to build a proper tree, we won't layout that
                return [];
            }

            let newPosition = -1;
            // Find the smallest position value of the parents
            for (const edge of current.edges) {
                if (edge.source == current) {
                    childCount[edge.target.getId()]--;
                    if (childCount[edge.target.getId()] == 0) {
                        phaseBWorkList.push(edge.target);
                    }
                } else {
                    if (newPosition == -1 || newPosition > positionTable[edge.source.getId()]) {
                        newPosition = positionTable[edge.source.getId()];
                    }
                }
            }
            if (newPosition != -1) {
                positionTable[current.getId()] = newPosition - 1;
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
        let gridSpace = Config.GRAPHICAL_EDITOR_GRID_SPACE;
        let maxHeight = Math.max(...dimTable.map(d => d.height));

        for (let layerIndex = 0; layerIndex < dimTable.length; layerIndex++) {
            const layerNodes = nodeOrdering[layerIndex];
            const layerDimensions = dimTable[layerIndex];
            let dynamicYOffset = (maxHeight - layerDimensions.height) / (layerNodes.length + 1);
            let yOffset = dynamicYOffset - (dynamicYOffset % gridSpace);
            for (let nodeIndex = 0; nodeIndex < layerNodes.length; nodeIndex++) {
                const node = layerNodes[nodeIndex];
                let nodeClearanceX = 0.5 * (layerDimensions.width - node.getGeometry().width);
                let nodeX = layerPositionX + nodeClearanceX;
                let nodeY = layerPositionY + yOffset;
                yOffset += node.getGeometry().height + Config.CEG_LAYOUT_CLEARANCE_Y + dynamicYOffset - (dynamicYOffset % gridSpace);
                let geometry = node.getGeometry().clone() as mxgraph.mxGeometry;
                geometry.x = nodeX;
                geometry.y = nodeY;
                this.graph.getModel().setGeometry(node, geometry);
            }
            layerPositionX += Config.CEG_LAYOUT_CLEARANCE_X + layerDimensions.width;
        }
    }
}
