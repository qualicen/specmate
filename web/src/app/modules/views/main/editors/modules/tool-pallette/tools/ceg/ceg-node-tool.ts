import { CEGNodeFactory } from '../../../../../../../../factory/ceg-node-factory';
import { ElementFactoryBase } from '../../../../../../../../factory/element-factory-base';
import { CEGModel } from '../../../../../../../../model/CEGModel';
import { CEGNode } from '../../../../../../../../model/CEGNode';
import { CreateNodeToolBase } from '../create-node-tool-base';
import { ShapeProvider } from '../../../graphical-editor/providers/properties/shape-provider';
import { Id } from 'src/app/util/id';
import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { Proxy } from 'src/app/model/support/proxy';
import { Type } from 'src/app/util/type';

export class CEGNodeTool extends CreateNodeToolBase<CEGNode> {

    protected modelType: { className: string; } = CEGModel;

    public icon = 'plus';
    public name = 'tools.addCegNode';
    public style = new ShapeProvider(CEGModel).getStyle(CEGNode);

    public copyfrom: CEGNode;

    protected getElementFactory(coords: { x: number; y: number; }): ElementFactoryBase<CEGNode> {
        return new CEGNodeFactory(coords, this.dataService, this.copyfrom);
    }

    public async perform(compoundId = Id.uuid): Promise<CEGNode> {
        this.copyfrom = this.value as CEGNode;
        const node = await super.perform(compoundId);
        if (this.value !== undefined && Type.is(this.value, CEGNode)) {
            const linkingNodeProxies = (this.value as CEGNode).linksFrom;
            for (const linkingNodeProxy of linkingNodeProxies) {
                const linkingNode = await this.dataService.readElement(linkingNodeProxy.url);
                const proxy = new Proxy();
                proxy.url = node.url;
                (linkingNode as CEGLinkedNode).linkTo = proxy;
                await this.dataService.updateElement(linkingNode, true, compoundId);
            }
        }
        return node;
    }
}
