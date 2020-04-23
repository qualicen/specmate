import * as he from 'he';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { CEGNode } from '../../../../../../../../model/CEGNode';
import { IContainer } from '../../../../../../../../model/IContainer';
import { IModelNode } from '../../../../../../../../model/IModelNode';
import { Type } from '../../../../../../../../util/type';
import { EditorStyle } from '../../components/editor-components/editor-style';
import { ConverterBase } from '../../converters/converter-base';
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

    private static INITIAL_CHILD_NODE_X = 0.5;
    private static EMPTY_CHILD_NODE_WIDTH = 50;

    constructor(element: IContainer, private graph: mxgraph.mxGraph,
        private shapeProvider: ShapeProvider, private nodeNameConverter: ConverterBase<any, CEGmxModelNode | string>) {
        super(element);
    }

    public provideCEGNode(url: string, x: number, y: number, width: number, height: number, data: CEGmxModelNode): mxgraph.mxCell {
        const value: string = null;
        const style = this.shapeProvider.getStyle(new CEGNode());
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();
        const vertex = this.graph.insertVertex(parent, url, value, x, y, width, height, style);
        const l1 = this.graph.insertVertex(vertex, url + VertexProvider.ID_SUFFIX_VARIABLE, data.variable,
            VertexProvider.INITIAL_CHILD_NODE_X, 0.15, 0, (mx.mxConstants.DEFAULT_FONTSIZE), EditorStyle.VARIABLE_NAME_STYLE, true);
        const l2 = this.graph.insertVertex(vertex, url + VertexProvider.ID_SUFFIX_CONDITION, data.condition,
            VertexProvider.INITIAL_CHILD_NODE_X, 0.4, 0, (mx.mxConstants.DEFAULT_FONTSIZE), EditorStyle.TEXT_INPUT_STYLE, true);
        const l3 = this.graph.insertVertex(vertex, url + VertexProvider.ID_SUFFIX_TYPE, data.type,
            VertexProvider.INITIAL_CHILD_NODE_X, 0.65, 0, (mx.mxConstants.DEFAULT_FONTSIZE), EditorStyle.TEXT_INPUT_STYLE, true);

        VertexProvider.adjustChildCellSize(l1, width);
        VertexProvider.adjustChildCellSize(l2, width);

        l1.isConnectable = () => false;
        l2.isConnectable = () => false;
        l3.isConnectable = () => false;
        this.graph.getModel().endUpdate();
        return vertex;
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

    public provideVertex(node: IModelNode, x?: number, y?: number): mxgraph.mxCell {
        const width = node.width > 0 ? node.width : this.shapeProvider.getInitialSize(node).width;
        const height = node.height > 0 ? node.height : this.shapeProvider.getInitialSize(node).height;

        if (Type.is(node, CEGNode)) {
            let n = node as CEGNode;
            const data = new CEGmxModelNode(n.variable, n.condition, n.type);
            return this.provideCEGNode(node.url, x || node.x, y || node.y, width, height, data);
        }

        const value: string = (this.nodeNameConverter ? this.nodeNameConverter.convertTo(node) : node.name) as string;
        const style = this.shapeProvider.getStyle(node);
        const parent = this.graph.getDefaultParent();
        const vertex = this.graph.insertVertex(parent, node.url, value, x || node.x, y || node.y, width, height, style);
        return vertex;
    }

    public static initRenderer(graph: mxgraph.mxGraph) {
        graph.convertValueToString = function (cell: mxgraph.mxCell) {
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
                mx.mxEvent.addListener(dropdown, 'change', (evt: mxgraph.mxEventObject) => {
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
            return he.encode(cell.value);
        };
    }

    public static isCEGTextInputCell(cell: mxgraph.mxCell): boolean {
        return [VertexProvider.ID_SUFFIX_VARIABLE, VertexProvider.ID_SUFFIX_CONDITION]
            .find(suffix => cell.id.endsWith(suffix)) !== undefined;
    }
}
