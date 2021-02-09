import { Config } from '../config/config';
import { CEGNode } from '../model/CEGNode';
import { IContainer } from '../model/IContainer';
import { SpecmateDataService } from '../modules/data/modules/data-service/services/specmate-data.service';
import { Id } from '../util/id';
import { Url } from '../util/url';
import { ElementFactoryBase } from './element-factory-base';
import { PositionableElementFactoryBase } from './positionable-element-factory-base';

export class CEGNodeFactory extends PositionableElementFactoryBase<CEGNode> {

    constructor(protected coords: { x: number; y: number; }, protected dataService: SpecmateDataService, private copyFrom?: CEGNode) {
        super(coords, dataService);
    }

    public create(parent: IContainer, commit: boolean, compoundId = Id.uuid, name?: string): Promise<CEGNode> {

        let id: string = Id.uuid;
        let url: string = Url.build([parent.url, id]);
        let node: CEGNode = new CEGNode();
        node.name = name || Config.CEG_NEW_NODE_NAME + ' ' + ElementFactoryBase.getDateStr();
        node.description = Config.CEG_NEW_NODE_DESCRIPTION;
        node.id = id;
        node.url = url;
        node.recycled = false;
        node.hasRecycledChildren = false;
        node.type = this.copyFrom?.type ?? Config.CEG_NODE_NEW_TYPE;
        node.variable = this.copyFrom?.variable ?? Config.CEG_NODE_NEW_VARIABLE;
        node.condition = this.copyFrom?.condition ?? Config.CEG_NODE_NEW_CONDITION;
        node.x = this.coords.x;
        node.y = this.coords.y;
        node.tracesFrom = this.copyFrom?.tracesFrom ?? [];
        node.tracesTo = this.copyFrom?.tracesTo ?? [];
        node.incomingConnections = this.copyFrom?.incomingConnections ?? [];
        node.outgoingConnections = this.copyFrom?.outgoingConnections ?? [];
        node.linksFrom = this.copyFrom?.linksFrom ?? [];

        return this.dataService.createElement(node, true, compoundId).then(() => node);
    }
}
