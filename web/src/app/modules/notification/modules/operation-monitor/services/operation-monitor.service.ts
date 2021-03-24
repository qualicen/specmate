import { EventEmitter, Injectable } from '@angular/core';
import { Config } from 'src/app/config/config';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { ValidationService } from 'src/app/modules/forms/modules/validation/services/validation.service';
import { GraphicalEditorService } from 'src/app/modules/views/main/editors/modules/graphical-editor/services/graphical-editor.service';
import { ModelImageService } from 'src/app/modules/views/main/editors/modules/graphical-editor/services/model-image.service';
import { Arrays } from 'src/app/util/arrays';
import { Monitorable } from '../base/monitorable';


enum DelayState {
    IDLE, START, DELAY
}

export enum ModalState {
    CLOSED, OPENING, OPEN
}

@Injectable()
export class OperationMonitorService {

    private static noModalOperations = [SpecmateDataService.OP_SEARCH];

    private _isLoading: boolean;

    private delayTimer: any;
    private modalTimer: any;
    private delayState: DelayState = DelayState.IDLE;
    private modalState: ModalState = ModalState.CLOSED;

    private subjects: Monitorable[] = [];

    public loadingStateChanged = new EventEmitter<boolean>();
    public modalStateChanged = new EventEmitter<boolean>();

    private activeOperations: Map<Monitorable, string[]> = new Map<Monitorable, string[]>();

    private _forceModalClosed: boolean;
    public set forceModalClosed(force: boolean) {
        this._forceModalClosed = force;
        if (force === true && this.modalState === ModalState.OPEN) {
            this.modalStateChanged.emit(false);
        } else if (force === false) {
            setTimeout(() => {
                if (this.modalState === ModalState.OPEN) {
                    this.modalStateChanged.emit(true);
                }
            }, Config.LOADING_MODAL_DELAY);

        }
    }

    constructor(dataService: SpecmateDataService,
        validationService: ValidationService,
        modelIageService: ModelImageService,
        graphicalEditorService: GraphicalEditorService) {

        this.subjects.push(dataService);
        this.subjects.push(validationService);
        this.subjects.push(modelIageService);
        this.subjects.push(graphicalEditorService);

        this.subjects.forEach((monitorable) => {
            this.activeOperations.set(monitorable, []);
            monitorable.onStart(operation => this.startOperation(monitorable, operation));
            monitorable.onEnd((operation) => this.endOperation(monitorable, operation));
        });
    }

    public startOperation(monitorable: Monitorable, name: string): void {
        this.activeOperations.get(monitorable).push(name);
        if (this.numActiveOperations === 1) {
            this.actOnStateChanged();
        }
    }

    public endOperation(monitorable: Monitorable, name: string): void {
        if (this.activeOperations.get(monitorable).indexOf(name) < 0) {
            return;
        }
        Arrays.remove(this.activeOperations.get(monitorable), name);
        if (this.numActiveOperations === 0) {
            this.actOnStateChanged();
        }
    }

    public get numActiveOperations(): number {
        let num = 0;
        this.subjects.forEach(monitorable => {
            num += this.activeOperations.get(monitorable).length;
        });
        return num;
    }

    private get hasActiveModalOperation(): boolean {
        let num = 0;
        this.subjects.forEach(monitorable => {
            num += this.activeOperations.get(monitorable)
                .filter(name => OperationMonitorService.noModalOperations.indexOf(name) < 0)
                .length;
        });
        return num > 0;
    }

    public get hasActiveOperation(): boolean {
        return this.numActiveOperations > 0;
    }

    private actOnStateChanged(): void {
        if (this.delayState === DelayState.IDLE) {
            if (this.hasActiveModalOperation) {
                this.delayState = DelayState.START;
                this.setModalOpening();
                this.isLoading = true;
            }
        } else if (this.delayState === DelayState.START) {
            if (!this.hasActiveModalOperation) {
                this.delayState = DelayState.DELAY;
                this.delayTimer = setTimeout(() => {
                    this.delayState = DelayState.IDLE;
                    this.setModalClosed();
                    this.isLoading = false;
                }, Config.LOADING_DEBOUNCE_DELAY);
            }
        } else if (this.delayState === DelayState.DELAY) {
            if (this.hasActiveModalOperation) {
                this.delayState = DelayState.START;
                clearTimeout(this.delayTimer);
            }
        }
    }

    private setModalOpening() {
        if (this.modalState === ModalState.CLOSED) {
            this.modalState = ModalState.OPENING;
            this.modalTimer = setTimeout(() => {
                this.modalState = ModalState.OPEN;
                if (this._forceModalClosed !== true) {
                    this.modalStateChanged.emit(true);
                }
            }, Config.LOADING_MODAL_DELAY);
        }
    }

    private setModalClosed() {
        if (this.modalState === ModalState.OPENING) {
            clearTimeout(this.modalTimer);
        }
        this.modalState = ModalState.CLOSED;
        this.modalStateChanged.emit(false);
    }

    private get isLoading(): boolean {
        return this._isLoading;
    }

    private set isLoading(isLoading: boolean) {
        this._isLoading = isLoading;
        this.loadingStateChanged.emit(this.isLoading);
    }
}
