import { CEGModel } from '../model/CEGModel';
import { IContainer } from '../model/IContainer';
import { Process } from '../model/Process';
import { Id } from '../util/id';
import { Url } from '../util/url';
import { ElementFactoryBase } from './element-factory-base';
import { Proxy } from '../model/support/proxy';
import { ModelImageFactory } from './model-image-factory';

export abstract class ModelFactoryBase extends ElementFactoryBase<CEGModel | Process> {

    public async create(parent: IContainer, commit: boolean, compoundId?: string, name?: string): Promise<CEGModel | Process> {
        let element: CEGModel | Process = this.simpleModel;
        element.id = Id.uuid;
        element.url = Url.build([parent.url, element.id]);
        element.name = name || this.name;
        element.description = this.description;
        element.recycled = false;
        element.hasRecycledChildren = false;
/*         const factory: ModelImageFactory = new ModelImageFactory(this.dataService);
        let modelImage = await factory.create(element, commit);
        element.image = new Proxy();
        element.image.url = modelImage.url; */

        return this.dataService.createElement(element, true, compoundId)
            .then(() => commit ? this.dataService.commit('create') : Promise.resolve())
            .then(() => element);
    }

    protected abstract get simpleModel(): CEGModel | Process;
    protected abstract get name(): string;
    protected abstract get description(): string;

}
