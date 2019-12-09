import { ProviderBase } from './provider-base';
import { IContainer } from '../../../../../../../../model/IContainer';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { IModelNode } from '../../../../../../../../model/IModelNode';
import { ShapeProvider} from './shape-provider';
import {CEGmxModelNode} from './ceg-mx-model-node';
import { ConverterBase } from '../../converters/converter-base';
import {VariableAndCondition} from '../../converters/variable-condition-name-converter';
import { Type } from '../../../../../../../../util/type';
import { CEGNode } from '../../../../../../../../model/CEGNode';

declare var require: any;

const mx: typeof mxgraph = require('mxgraph')({
    mxBasePath: 'mxgraph'
});

/**
 * Based on https://github.com/jgraph/mxgraph/blob/master/javascript/examples/editing.html
 */
export class VertexProvider extends ProviderBase {
    constructor(element: IContainer, private graph: mxgraph.mxGraph,
        private shapeProvider: ShapeProvider, private nodeNameConverter: ConverterBase<any, CEGmxModelNode|string>) {
        super(element);
    }

    public provideCEGNode(url: string, x: number, y: number, width: number, height: number, data: CEGmxModelNode): mxgraph.mxCell {
        const value: string = null;
        const style = this.shapeProvider.getStyle(new CEGNode());
        const parent = this.graph.getDefaultParent();
        this.graph.getModel().beginUpdate();
        const vertex = this.graph.insertVertex(parent, url, value, x, y, width, height, style);
        const l1 = this.graph.insertVertex(vertex, url + '/variable' , data.variable, 0.5, 0.25, 0, 0, null, true);
        const l2 = this.graph.insertVertex(vertex, url + '/condition' , data.condition, 0.5, 0.5, 0, 0, null, true);
        const l3 = this.graph.insertVertex(vertex, url + '/type', data.type, 0.5, 0.75, 0, 0, null, true);
        l1.isConnectable = () => false;
        l2.isConnectable = () => false;
        l3.isConnectable = () => false;
        this.graph.getModel().endUpdate();
        return vertex;
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

    public initCEGRenderer(graph: mxgraph.mxGraph) {
        graph.convertValueToString = function(cell: mxgraph.mxCell) {
            if (cell.getId().endsWith('/type')) {
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

                cell.valueChanged = function(newValue: any) {
                    let sel = optionElements.find(e => e.value === newValue);
                    if (sel !== undefined) {
                        sel.setAttribute('selected', 'true');
                    }
                    mx.mxCell.prototype.valueChanged.bind(cell)(newValue);
                };
                return dropdown;
            }
            return mx.mxGraph.prototype.convertValueToString.bind(graph)(cell);
        };
    }
}
