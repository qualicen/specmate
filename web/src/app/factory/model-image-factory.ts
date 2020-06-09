import { IContainer } from '../model/IContainer';
import { Id } from '../util/id';
import { Url } from '../util/url';
import { ElementFactoryBase } from './element-factory-base';
import { ModelImage } from '../model/ModelImage';

export class ModelImageFactory extends ElementFactoryBase<ModelImage> {

    public create(parent: IContainer, commit: boolean, compoundId?: string, name?: string): Promise<ModelImage> {
        compoundId = compoundId || Id.uuid;
        let element: ModelImage = new ModelImage();
        element.id = Id.uuid;
        element.url = Url.build([parent.url, element.id]);
        element.name = name || parent.name + '-image';
        element.description = '';
        element.recycled = false;
        element.hasRecycledChildren = false;
        element.imageData = '';

        return this.dataService.createElement(element, true, compoundId)
            .then(() => commit ? this.dataService.commit('create') : Promise.resolve())
            .then(() => element);
    }
}
