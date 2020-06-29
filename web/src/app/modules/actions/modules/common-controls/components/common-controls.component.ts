import { Component } from '@angular/core';
import { TranslateService } from '@ngx-translate/core';
import { ServerConnectionService } from '../../../../common/modules/connection/services/server-connection-service';
import { UISafe } from '../../../../common/modules/ui/ui-safe-decorator';
import { SpecmateDataService } from '../../../../data/modules/data-service/services/specmate-data.service';
import { ValidationService } from '../../../../forms/modules/validation/services/validation.service';
import { NavigatorService } from '../../../../navigation/modules/navigator/services/navigator.service';
import { UndoService } from '../services/undo.service';
import { ConfirmationModal } from 'src/app/modules/notification/modules/modals/services/confirmation-modal.service';
import { CEGModel } from 'src/app/model/CEGModel';
import { Process } from 'src/app/model/Process';
import { Type } from 'src/app/util/type';
import { ModelImageService } from 'src/app/modules/views/main/editors/modules/graphical-editor/services/model-image.service';

@Component({
    moduleId: module.id.toString(),
    selector: 'common-controls',
    templateUrl: 'common-controls.component.html',
    styleUrls: ['common-controls.component.css']
})
export class CommonControls {

    public get connected(): boolean {
        return this.connection.isConnected;
    }

    constructor(
        private dataService: SpecmateDataService,
        private connection: ServerConnectionService,
        private validator: ValidationService,
        private navigator: NavigatorService,
        private translate: TranslateService,
        private undoService: UndoService,
        private modal: ConfirmationModal,
        private modelImageService: ModelImageService) {
    }

    public async save(): Promise<void> {
        if (this.isSaveEnabled) {
            await this.validator.validateCurrent();
            if (this.isSaveEnabled && this.validator.isSavingEnabled()) {
                if (this.isModel()) {
                    await this.modelImageService.createModelImage(this.navigator.currentElement);
                }
                this.dataService.commit(this.translate.instant('save'));
            } else {
                let message = this.translate.instant('saveError.message') + '\n' + this.validator.getValidationResultAsString(true);
                this.modal.openOk(this.translate.instant('saveError.title'), message);
            }
        }
    }

    public close(): void {
        this.back();
    }

    public undo(): void {
        if (!this.isModel()) {
            this.dataService.undo();
        }
        this.undoService.undo();

    }

    public redo(): void {
        this.undoService.redo();
    }

    public forward(): void {
        if (this.isForwardEnabled) {
            this.navigator.forward();
        }
    }

    public back(): void {
        if (this.isBackEnabled) {
            this.navigator.back();
        }
    }

    public cancelEvent(event: Event): void {
        event.preventDefault();
        event.stopPropagation();
    }

    public get isSaveEnabled(): boolean {
        return this.isEnabled && this.hasCommits;
    }

    public get isUndoEnabled(): boolean {
        if (this.isModel()) {
            return this.undoService.isUndoEnabled();
        }
        return this.hasCommits;
    }

    public get isRedoEnabled(): boolean {
        return this.undoService.isRedoEnabled();
    }

    @UISafe()
    private get hasCommits(): boolean {
        return this.dataService.hasCommits;
    }

    public get isBackEnabled(): boolean {
        return this.navigator.hasPrevious;
    }

    public get isForwardEnabled(): boolean {
        return this.navigator.hasNext;
    }

    public get isEnabled(): boolean {
        return true;
    }
    private isModel(): boolean {
        return Type.is(this.navigator.currentElement, CEGModel) || Type.is(this.navigator.currentElement, Process);
    }
}
