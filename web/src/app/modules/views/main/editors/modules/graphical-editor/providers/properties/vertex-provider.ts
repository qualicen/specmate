import * as he from 'he';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { ProcessNode } from 'src/app/model/ProcessNode';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { CEGLinkedNode } from '../../../../../../../../model/CEGLinkedNode';
import { CEGNode } from '../../../../../../../../model/CEGNode';
import { IContainer } from '../../../../../../../../model/IContainer';
import { IModelNode } from '../../../../../../../../model/IModelNode';
import { Type } from '../../../../../../../../util/type';
import { EditorStyle } from '../../components/editor-components/editor-style';
import { StyleChanger } from '../../components/util/style-changer';
import { ConverterBase } from '../../converters/converter-base';
import { ChangeGuardService } from '../../services/change-guard.service';
import { CEGmxModelNode } from './ceg-mx-model-node';
import { ProviderBase } from './provider-base';
import { ShapeProvider } from './shape-provider';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

/**
 * Based on https://github.com/jgraph/mxgraph/blob/master/javascript/examples/editing.html
 */
export class VertexProvider extends ProviderBase {

    public static ID_SUFFIX_VARIABLE = '/variable';
    public static ID_SUFFIX_CONDITION = '/condition';
    public static ID_SUFFIX_TYPE = '/type';
    public static ID_SUFFIX_LINK_ICON = '/link-icon';

    private static INITIAL_CHILD_NODE_X = 0.5;
    private static EMPTY_CHILD_NODE_WIDTH = 50;

    constructor(element: IContainer,
        private graph: mxgraph.mxGraph,
        private shapeProvider: ShapeProvider,
        private nodeNameConverter: ConverterBase<any, CEGmxModelNode | string>,
        private dataService: SpecmateDataService,
        private changeGuard: ChangeGuardService) {
        super(element);
    }

    public provideCEGNode(url: string, x: number, y: number, width: number, height: number,
        data: CEGmxModelNode, node: CEGNode): mxgraph.mxCell {
        const value = node;
        const style = this.shapeProvider.getStyle(new CEGNode());
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();
        const vertex = this.graph.insertVertex(parent, url, data, x, y, width, height, EditorStyle.BASE_CEG_NODE_STYLE);
        //const vertex = this.graph.insertVertex(parent, url, data, x, y, width, height, null);

        this.graph.getModel().endUpdate();
        return vertex;
    }

    public provideLinkedCEGNode(url: string, x: number, y: number, width: number, height: number,
        data: CEGmxModelNode, node: CEGLinkedNode): mxgraph.mxCell {

        const value = node;
        const style = this.shapeProvider.getStyle(CEGLinkedNode);
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();
        const vertex = this.graph.insertVertex(parent, url, value, x, y, width, height, style);
        const vertexVariable = this.graph.insertVertex(vertex, url + VertexProvider.ID_SUFFIX_VARIABLE, data.variable,
            // tslint:disable-next-line: max-line-length
            VertexProvider.INITIAL_CHILD_NODE_X, 0.15, 0, (mx.mxConstants.DEFAULT_FONTSIZE), EditorStyle.VARIABLE_NAME_DISABLED_STYLE, true);
        const vertexCondition = this.graph.insertVertex(vertex, url + VertexProvider.ID_SUFFIX_CONDITION, data.condition,
            VertexProvider.INITIAL_CHILD_NODE_X, 0.4, 0, (mx.mxConstants.DEFAULT_FONTSIZE), EditorStyle.TEXT_INPUT_DISABLED_STYLE, true);
        const vertexSymbol = this.graph.insertVertex(vertex, url + VertexProvider.ID_SUFFIX_LINK_ICON, '🔗',
            8, 8, 0, (mx.mxConstants.DEFAULT_FONTSIZE), EditorStyle.ICON_STYLE, false);

        this.addEmptyTextStyle(data.variable, vertexVariable);
        this.addEmptyTextStyle(data.condition, vertexCondition);

        vertexVariable.isConnectable = () => false;
        vertexCondition.isConnectable = () => false;
        vertexSymbol.isConnectable = () => false;

        VertexProvider.adjustChildrenCellSizes(vertex, this.shapeProvider, this.graph);
        this.graph.getModel().endUpdate();
        return vertex;
    }

    private addEmptyTextStyle(text: string, cell: mxgraph.mxCell) {
        if (text === '' || text === undefined || text === null) {
            StyleChanger.addStyle(cell, this.graph, EditorStyle.EMPTY_TEXT_NAME);
        }
    }

    public static adjustChildCellSize(cell: mxgraph.mxCell, nodeWidth: number) {
        const g = cell.getGeometry();
        let x = VertexProvider.INITIAL_CHILD_NODE_X;
        let w = 0;
        const minWidth = VertexProvider.EMPTY_CHILD_NODE_WIDTH;
        if (g.width < minWidth && (cell.value === '' || cell.value === null || cell.value === undefined)) {
            x = x - (minWidth / nodeWidth) / 2;
            w = minWidth;
        }
        cell.getGeometry().setRect(x, g.y, w, g.height);
    }

    public static adjustChildrenPositions(cell: mxgraph.mxCell) {
        const g = cell.getGeometry();
        let parentWidth = g.width;
        if (cell.children !== undefined && cell.children !== null) {
            for (const child of cell.children) {
                const childGeometry = child.getGeometry();
                let x = (parentWidth - childGeometry.width) / 2 / parentWidth;
                child.getGeometry().setRect(x, childGeometry.y, childGeometry.width, childGeometry.height);
            }
        }
    }

    public static adjustChildrenCellSizes(cell: mxgraph.mxCell, shapeProvider: ShapeProvider, graph: mxgraph.mxGraph) {
        const g = cell.getGeometry();
        let parentWidth = g.width;
        if (cell.children !== undefined && cell.children !== null) {
            for (const child of cell.children) {
                if (child.getId().endsWith(VertexProvider.ID_SUFFIX_TYPE)) {
                    continue;
                }
                const childGeometry = child.getGeometry();
                let preferredSize = graph.getPreferredSizeForCell(child);
                let shapedata = shapeProvider.getInitialData(cell.style.split(';')[0]);
                let widthMax = parentWidth - shapedata.size.margin;
                let width = Math.min(preferredSize.width, widthMax);
                child.getGeometry().setRect(childGeometry.x, childGeometry.y, width, childGeometry.height);
            }
        }
        VertexProvider.adjustChildrenPositions(cell);
    }

    public async provideVertex(node: IModelNode, x?: number, y?: number): Promise<mxgraph.mxCell> {
        const width = node.width > 0 ? node.width : this.shapeProvider.getInitialSize(node).width;
        const height = node.height > 0 ? node.height : this.shapeProvider.getInitialSize(node).height;

        if (Type.is(node, CEGNode)) {
            let n = node as CEGNode;
            const data = new CEGmxModelNode(n.variable, n.condition, n.type);
            return this.provideCEGNode(node.url, x || node.x, y || node.y, width, height, data, node as CEGNode);
        }
        if (Type.is(node, CEGLinkedNode)) {
            let n = node as CEGLinkedNode;
            let linkedNode = undefined;
            let variable = '';
            let condition = '';
            let type = '';
            if (n.linkTo !== undefined) {
                linkedNode = await this.dataService.readElement(n.linkTo.url) as CEGNode;
                if (linkedNode !== undefined) {
                    variable = linkedNode.variable;
                    condition = linkedNode.condition;
                    type = linkedNode.type;
                }
            }
            const data = new CEGmxModelNode(variable, condition, type);
            return this.provideLinkedCEGNode(node.url, x || node.x, y || node.y, width, height, data, node as CEGLinkedNode);
        }

        const value: string = (this.nodeNameConverter ? this.nodeNameConverter.convertTo(node) : node.name) as string;
        const style = this.shapeProvider.getStyle(node);
        const parent = this.graph.getDefaultParent();
        const vertex = this.graph.insertVertex(parent, node.url, value, x || node.x, y || node.y, width, height, style);
        return vertex;
    }

    public initRenderer(graph: mxgraph.mxGraph) {
        /* graph.convertValueToString = (cell: mxgraph.mxCell) => {
            if (cell.getId().endsWith(VertexProvider.ID_SUFFIX_TYPE)) {
                let parent = cell.getParent();
                let edges = parent.edges;
                if (edges === undefined || edges === null) {
                    return;
                }
                let inDegree = edges.filter(value => value.target === parent).length;
                if (inDegree < 2) {
                    return '';
                }
                let dropdown = document.createElement('select');
                let options = ['AND', 'OR'];
                let optionElements: HTMLOptionElement[] = [];
                for (const option of options) {
                    let optionElem = document.createElement('option');
                    optionElem.innerHTML = option;
                    optionElem.setAttribute('value', option);
                    if (option === cell.getValue()) {
                        optionElem.setAttribute('selected', 'true');
                        dropdown.value = option;
                    }
                    dropdown.appendChild(optionElem);
                    optionElements.push(optionElem);
                }

                mx.mxEvent.addListener(dropdown, 'click', async (evt: MouseEvent) => {
                    const element = await this.dataService.readElement(parent.id, true);
                    const guardResult = await this.changeGuard.guardSelectedElements([element]);
                    if (!guardResult) {
                        evt.stopPropagation();
                        evt.preventDefault();
                    }
                });

                mx.mxEvent.addListener(dropdown, 'change', async (evt: mxgraph.mxEventObject) => {
                    graph.model.setValue(cell, dropdown.value);
                });

                cell.valueChanged = function (newValue: any) {
                    let sel = optionElements.find(e => e.value === newValue);
                    if (sel !== undefined) {
                        sel.setAttribute('selected', 'true');
                    }
                    return mx.mxCell.prototype.valueChanged.bind(cell)(newValue);
                };
                return dropdown;
            }
            return mx.mxGraph.prototype.convertValueToString.bind(graph)(cell);
        };

        graph.getLabel = function (cell: mxgraph.mxCell) {
            if (VertexProvider.isCEGTextInputCell(cell)) {
                if (cell.value !== undefined && cell.value !== null) {
                    return he.encode(cell.value);
                }
            }
            return mx.mxGraph.prototype.getLabel.bind(graph)(cell);
        };

        graph.getTooltipForCell = (cell) => {
            if (cell.getId().endsWith(VertexProvider.ID_SUFFIX_TYPE) || cell.value === null || cell.value === undefined) {
                return '';

            }
            return he.encode(cell.value + '').replace('[object Object]', '');
        }; */
    }

    public static isCEGTextInputCell(cell: mxgraph.mxCell): boolean {
        return [VertexProvider.ID_SUFFIX_VARIABLE, VertexProvider.ID_SUFFIX_CONDITION]
            .find(suffix => cell.id.endsWith(suffix)) !== undefined;
    }
}
