import { Component, Input, OnInit } from '@angular/core';
import { LinkingDialogComponent } from '../../../linking-dialog/components/linking-dialog/linking-dialog.component';
import { NgbModalRef } from '@ng-bootstrap/ng-bootstrap';
import { ModalService } from '../../../../../notification/modules/modals/services/modal-service';
import { IContainer } from 'src/app/model/IContainer';
import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { Type } from 'src/app/util/type';
import { Proxy } from 'src/app/model/support/proxy';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { Id } from 'src/app/util/id';
import { CEGNode } from 'src/app/model/CEGNode';

@Component({
    selector: 'link-model-button',
    templateUrl: './link-model-button.component.html',
    styleUrls: ['./link-model-button.component.css']
})
export class LinkModelButtonComponent implements OnInit {

    private dialogOpen = false;
    private modalRef: NgbModalRef;
    private _element: CEGLinkedNode;

    constructor(private modalService: ModalService,
        private dataService: SpecmateDataService) {
    }

    ngOnInit(): void {
    }

    @Input()
    public set element(element: IContainer) {
        if (!element || !Type.is(element, CEGLinkedNode)) {
            return;
        }
        this._element = element as CEGLinkedNode;
    }

    openLinkingDialog(): void {
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

            this.modalRef.result.then(async (link) => {
                this.dialogOpen = false;
                let oldLinkedNodeUrl = this._element.linkTo.url;
                let oldLinkedNode: CEGNode = await this.dataService.readElement(oldLinkedNodeUrl) as CEGNode;
                oldLinkedNode.linksFrom = oldLinkedNode.linksFrom.filter(element => element.url !== this._element.url);
                this._element.linkTo = new Proxy();
                this._element.linkTo.url = link.url;
                let proxy = new Proxy();
                proxy.url = this._element.url;
                link.linksFrom.push(proxy);
                this._element.name = link.variable + ' ' + link.condition;
                this.dataService.updateElement(oldLinkedNode, true, Id.uuid);
                this.dataService.updateElement(link, true, Id.uuid);
                this.dataService.updateElement(this._element, true, Id.uuid);
            }).catch(() => {
                this.dialogOpen = false;
            });
        }
    }
}
