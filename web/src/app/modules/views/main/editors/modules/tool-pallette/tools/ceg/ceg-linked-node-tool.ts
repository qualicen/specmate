import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { CEGNode } from 'src/app/model/CEGNode';
import { IContainer } from 'src/app/model/IContainer';
import { Proxy } from 'src/app/model/support/proxy';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { LinkingDialogComponent } from 'src/app/modules/linking/modules/linking-dialog/components/linking-dialog/linking-dialog.component';
import { ModalService } from 'src/app/modules/notification/modules/modals/services/modal-service';
import { SelectedElementService } from 'src/app/modules/views/side/modules/selected-element/services/selected-element.service';
import { Id } from 'src/app/util/id';
import { Type } from 'src/app/util/type';
import { CEGLinkedNodeFactory } from '../../../../../../../../factory/ceg-linked-node-factory';
import { ElementFactoryBase } from '../../../../../../../../factory/element-factory-base';
import { CEGLinkedNode } from '../../../../../../../../model/CEGLinkedNode';
import { CEGModel } from '../../../../../../../../model/CEGModel';
import { ShapeProvider } from '../../../graphical-editor/providers/properties/shape-provider';
import { CreateNodeToolBase } from '../create-node-tool-base';

export class CEGLinkedNodeTool extends CreateNodeToolBase<CEGLinkedNode> {

    protected modelType: { className: string; } = CEGModel;

    public icon = 'plus';
    public name = 'tools.addCegLinkedNode';
    public style = new ShapeProvider(CEGModel).getStyle(CEGLinkedNode);
    private dialogOpen = false;
    private modalRef: NgbModalRef;

    constructor(protected dataService: SpecmateDataService,
        protected selectedElementService: SelectedElementService,
        protected parent: IContainer,
        private modalService: ModalService) {
        super(dataService, selectedElementService, parent);

    }

    public async perform(compoundId = Id.uuid): Promise<CEGLinkedNode> {
        let node = await super.perform();
        let linkedNode: CEGNode = undefined;
        let valueLink = (this.value as CEGLinkedNode)?.linkTo;
        if (this.value !== undefined && Type.is(this.value, CEGLinkedNode) && valueLink !== undefined && valueLink !== null
            && valueLink.url !== undefined && valueLink.url !== null) {
            linkedNode = await this.dataService.readElement(valueLink.url, true) as CEGNode;
        } else {
            linkedNode = await this.getLinkedNodeWithDialog(node);
        }

        if (linkedNode === undefined) {
            return undefined;
        }

        node.linkTo = new Proxy();
        node.linkTo.url = linkedNode.url;
        let proxy = new Proxy();
        proxy.url = node.url;
        if (linkedNode.linksFrom === undefined) {
            linkedNode.linksFrom = [];
        }
        linkedNode.linksFrom.push(proxy);
        node.name = linkedNode.variable + ' ' + linkedNode.condition;
        this.dataService.updateElement(linkedNode, true, compoundId);
        this.dataService.updateElement(node, true, compoundId);
        return node;
    }

    protected getElementFactory(coords: { x: number; y: number; }): ElementFactoryBase<CEGLinkedNode> {
        return new CEGLinkedNodeFactory(coords, this.dataService);
    }

    private async getLinkedNodeWithDialog(node: IContainer): Promise<CEGNode> {
        if (!this.dialogOpen) {
            this.dialogOpen = true;
            this.modalRef = this.modalService.open(LinkingDialogComponent, {
                // large size
                size: 'lg',
                // don't allow to hide the modal by clicking the background
                backdrop: 'static',
                // don't allow to hide the modal by hitting ESC
                keyboard: true,
                // center modal vertically
                centered: true
            });

            try {
                this.dialogOpen = false;
                return await this.modalRef.result;
            } catch {
                this.dialogOpen = false;
                await this.dataService.deleteElement(node.url, true, Id.uuid);
            }
        }
    }
}
