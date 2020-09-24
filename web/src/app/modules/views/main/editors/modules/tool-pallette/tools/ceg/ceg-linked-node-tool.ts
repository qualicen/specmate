import { CEGLinkedNodeFactory } from '../../../../../../../../factory/ceg-linked-node-factory';
import { ElementFactoryBase } from '../../../../../../../../factory/element-factory-base';
import { CEGModel } from '../../../../../../../../model/CEGModel';
import { CEGLinkedNode } from '../../../../../../../../model/CEGLinkedNode';
import { CreateNodeToolBase } from '../create-node-tool-base';
import { ShapeProvider } from '../../../graphical-editor/providers/properties/shape-provider';

export class CEGLinkedNodeTool extends CreateNodeToolBase<CEGLinkedNode> {

    protected modelType: { className: string; } = CEGModel;

    public icon = 'plus';
    public name = 'tools.addCegLinkedNode';
    public style = new ShapeProvider(CEGModel).getStyle(CEGLinkedNode);

    protected getElementFactory(coords: { x: number; y: number; }): ElementFactoryBase<CEGLinkedNode> {
        return new CEGLinkedNodeFactory(coords, this.dataService);
    }
}
