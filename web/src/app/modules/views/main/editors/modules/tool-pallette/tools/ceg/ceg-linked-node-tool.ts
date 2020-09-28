import { CEGLinkedNodeFactory } from '../../../../../../../../factory/ceg-linked-node-factory';
import { ElementFactoryBase } from '../../../../../../../../factory/element-factory-base';
import { CEGModel } from '../../../../../../../../model/CEGModel';
import { CEGLinkedNode } from '../../../../../../../../model/CEGLinkedNode';
import { CreateNodeToolBase } from '../create-node-tool-base';
import { ShapeProvider } from '../../../graphical-editor/providers/properties/shape-provider';
import { ModalService } from 'src/app/modules/notification/modules/modals/services/modal-service';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { IContainer } from 'src/app/model/IContainer';
import { LinkingDialogComponent } from 'src/app/modules/linking/modules/linking-dialog/components/linking-dialog/linking-dialog.component';
import { SelectedElementService } from 'src/app/modules/views/side/modules/selected-element/services/selected-element.service';
import { CEGNode } from 'src/app/model/CEGNode';
import { Proxy } from 'src/app/model/support/proxy';
import { Id } from 'src/app/util/id';
import { OnInit } from '@angular/core';

export class CEGLinkedNodeTool extends CreateNodeToolBase<CEGLinkedNode> implements OnInit {

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
    ngOnInit(): void {
    }

    public async perform(): Promise<CEGLinkedNode> {
        let node = await super.perform();
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

            this.modalRef.result.then((link: CEGNode) => {
                // the resulting link
                this.dialogOpen = false;
                node.linkTo = new Proxy();
                node.linkTo.url = link.url;
                let proxy = new Proxy();
                proxy.url = node.url;
                link.linksFrom.push(proxy);
                node.name = link.variable + ' ' + link.condition;
                this.dataService.updateElement(link, true, Id.uuid);
                this.dataService.updateElement(node, true, Id.uuid);
            }).catch(async () => {
                this.dialogOpen = false;
                await this.dataService.deleteElement(node.url, true, Id.uuid);
            });
        }
        return node;
    }

    protected getElementFactory(coords: { x: number; y: number; }): ElementFactoryBase<CEGLinkedNode> {
        return new CEGLinkedNodeFactory(coords, this.dataService);
    }


}
