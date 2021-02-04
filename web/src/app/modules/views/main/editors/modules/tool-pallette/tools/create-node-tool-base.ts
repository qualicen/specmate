import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { CEGNode } from 'src/app/model/CEGNode';
import { IContainer } from 'src/app/model/IContainer';
import { Id } from 'src/app/util/id';
import { ElementFactoryBase } from '../../../../../../../factory/element-factory-base';
import { IModelNode } from '../../../../../../../model/IModelNode';
import { CreateToolBase } from './create-tool-base';

export abstract class CreateNodeToolBase<T extends IModelNode> extends CreateToolBase {

    public color = 'primary';
    public isVertexTool = true;
    public isHidden = false;

    public coords: { x: number, y: number };
    public name: string;
    public value: CEGLinkedNode;

    public async perform(compoundId = Id.uuid): Promise<T> {
        if (this.coords === undefined) {
            throw new Error('Coords undefined');
        }
        return await this.createNewNodeAtCoords(compoundId);
    }

    private async createNewNodeAtCoords(compoundId = Id.uuid): Promise<T> {
        if (this.name === undefined || this.coords === undefined) {
            throw new Error('Necessary data undefined.');
        }
        const factory = this.getElementFactory(this.coords);
        const node = await factory.create(this.parent, false, compoundId, this.name);
        this.selectedElementService.select(node);
        return node;
    }

    protected abstract getElementFactory(coords: { x: number, y: number }): ElementFactoryBase<T>;
}
