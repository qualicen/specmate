import { ProviderBase } from './provider-base';
import { IContainer } from '../../../../../../../../model/IContainer';
import { mxgraph } from 'mxgraph'; // Typings only - no code!
import { IModelNode } from '../../../../../../../../model/IModelNode';
import { ShapeProvider} from './shape-provider';
import {ValuePair} from './value-pair';
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
          private shapeProvider: ShapeProvider, private nodeNameConverter: ConverterBase<any, ValuePair|string>) {
      super(element);
  }

  public provideCEGNode(url: string, x: number, y: number, width: number, height: number, data: ValuePair): mxgraph.mxCell {
    const value: string = null;
    const style = this.shapeProvider.getStyle(new CEGNode());
    const parent = this.graph.getDefaultParent();
    const vertex = this.graph.insertVertex(parent, url, value, x, y, width, height, style);
    const l1 = this.graph.insertVertex(vertex, url + '/variable' , data.variable, 0.5, 0.25, 0, 0, null, true);
    const l2 = this.graph.insertVertex(vertex, url + '/condition' , data.condition, 0.5, 0.75, 0, 0, null, true);
    l1.isConnectable = () => false;
    console.log(l1.getGeometry());
    l2.isConnectable = () => false;
    return vertex;
  }


  public provideVertex(node: IModelNode, x?: number, y?: number): mxgraph.mxCell {
    const width = node.width > 0 ? node.width : this.shapeProvider.getInitialSize(node).width;
    const height = node.height > 0 ? node.height : this.shapeProvider.getInitialSize(node).height;

    if (Type.is(node, CEGNode)) {
      let n = node as CEGNode;
      const data = new ValuePair(n.variable, n.condition);
      return this.provideCEGNode(node.url, x || node.x, y || node.y, width, height, data);
    }

    const value: string = (this.nodeNameConverter ? this.nodeNameConverter.convertTo(node) : node.name) as string;
    const style = this.shapeProvider.getStyle(node);
    const parent = this.graph.getDefaultParent();
    const vertex = this.graph.insertVertex(parent, node.url, value, x || node.x, y || node.y, width, height, style);
    if (Type.is(node, CEGNode)) {
        let n = node as CEGNode;
        const l1 = this.graph.insertVertex(vertex, node.url + '/variable' , n.variable, 0.5, 0.25, 0, 0, null, true);
        const l2 = this.graph.insertVertex(vertex, node.url + '/condition' , n.condition, 0.5, 0.75, 0, 0, null, true);
        l1.isConnectable = () => false;
        l2.isConnectable = () => false;
    }
    return vertex;
  }
}
