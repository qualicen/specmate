import { CEGNodeFactory } from '../../../../../../../../factory/ceg-node-factory';
import { ElementFactoryBase } from '../../../../../../../../factory/element-factory-base';
import { CEGModel } from '../../../../../../../../model/CEGModel';
import { CEGNode } from '../../../../../../../../model/CEGNode';
import { CreateNodeToolBase } from '../create-node-tool-base';
import { ShapeProvider } from '../../../graphical-editor/providers/properties/shape-provider';

export class CEGNodeTool extends CreateNodeToolBase<CEGNode> {

    protected modelType: { className: string; } = CEGModel;

    public icon = 'plus';
    public name = 'tools.addCegNode';
    public style = new ShapeProvider(CEGModel).getStyle(CEGNode);

    protected getElementFactory(coords: { x: number; y: number; }): ElementFactoryBase<CEGNode> {
        return new CEGNodeFactory(coords, this.dataService);
    }
}
