import { Component, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { CEGModel } from 'src/app/model/CEGModel';
import { CEGNode } from 'src/app/model/CEGNode';
import { Subject } from 'rxjs';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { ElementProvider } from 'src/app/modules/views/main/editors/modules/graphical-editor/providers/properties/element-provider';
import { TranslateService } from '@ngx-translate/core';
import { Type } from 'src/app/util/type';

@Component({
    moduleId: module.id.toString(),
    templateUrl: './linking-dialog.component.html',
    styleUrls: ['./linking-dialog.component.css']
})
export class LinkingDialogComponent implements OnInit {

    constructor(protected activeModal: NgbActiveModal,
        private dataService: SpecmateDataService) { }

    ngOnInit(): void {
        this.hasLink.next(false);
    }

    close(): void {
        this.activeModal.dismiss();
    }

    link(): void {
        this.activeModal.close(this.selectedEffect);
    }

    selectedModel: CEGModel = null;
    effects: Subject<CEGNode[]> = new Subject();
    selectedEffect: CEGNode = null;
    hasLink: Subject<boolean> = new Subject();

    updateModel(model: CEGModel) {
        this.selectedModel = null;
        if (model !== undefined && model !== null) {
            this.selectedModel = model;
            this.loadEffects();
        } else {
            this.effects.next([]);
        }
    }

    private async loadEffects() {
        const contents = await this.dataService.readContents(this.selectedModel.url, true);
        const elementProvider = new ElementProvider(this.selectedModel, contents);
        const newEffects = elementProvider.nodes
            .filter(n => Type.is(n, CEGNode))
            .map(n => n as CEGNode)
            .filter(n => n.outgoingConnections.length == 0);
        this.effects.next(newEffects);
    }

    updateEffect(effect: CEGNode) {
        if (effect !== undefined && effect !== null) {
            this.selectedEffect = effect;
            this.hasLink.next(true);
        } else {
            this.selectedEffect = null;
            this.hasLink.next(false);
        }
    }
}
