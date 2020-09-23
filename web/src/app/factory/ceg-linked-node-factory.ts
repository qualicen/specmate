import { Config } from '../config/config';
import { CEGLinkedNode } from '../model/CEGLinkedNode';
import { IContainer } from '../model/IContainer';
import { Id } from '../util/id';
import { Url } from '../util/url';
import { ElementFactoryBase } from './element-factory-base';
import { PositionableElementFactoryBase } from './positionable-element-factory-base';

export class CEGLinkedNodeFactory extends PositionableElementFactoryBase<CEGLinkedNode> {

    public create(parent: IContainer, commit: boolean, compoundId?: string, name?:
         string): Promise<CEGLinkedNode> {

        compoundId = compoundId || Id.uuid;

        let id: string = Id.uuid;
        let url: string = Url.build([parent.url, id]);
        let node: CEGLinkedNode = new CEGLinkedNode();
        node.name = name || Config.CEG_NEW_NODE_NAME + ' ' + ElementFactoryBase.getDateStr();
        node.description = Config.CEG_NEW_NODE_DESCRIPTION;
        node.id = id;
        node.url = url;
        node.recycled = false;
        node.hasRecycledChildren = false;
        node.x = this.coords.x;
        node.y = this.coords.y;
        node.tracesFrom = [];
        node.tracesTo = [];
        node.incomingConnections = [];
        node.outgoingConnections = [];
        node.linkTo = undefined;

        return this.dataService.createElement(node, true, compoundId).then(() => node);
    }
}
