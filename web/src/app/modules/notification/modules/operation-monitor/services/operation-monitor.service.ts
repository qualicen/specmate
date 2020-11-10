import { EventEmitter, Injectable } from '@angular/core';
import { SpecmateDataService } from 'src/app/modules/data/modules/data-service/services/specmate-data.service';
import { ValidationService } from 'src/app/modules/forms/modules/validation/services/validation.service';
import { ModelImageService } from 'src/app/modules/views/main/editors/modules/graphical-editor/services/model-image.service';
import { Arrays } from 'src/app/util/arrays';
import { Monitorable } from '../base/monitorable';

@Injectable()
export class OperationMonitorService {

    private subjects: Monitorable[] = [];

    public stateChanged = new EventEmitter<boolean>();

    private activeOperations: Map<Monitorable, string[]> = new Map<Monitorable, string[]>();

    constructor(dataService: SpecmateDataService, validationService: ValidationService, modelIageService: ModelImageService) {
        this.subjects.push(dataService);
        this.subjects.push(validationService);
        this.subjects.push(modelIageService);

        this.subjects.forEach((monitorable) => {
            this.activeOperations.set(monitorable, []);
            monitorable.onStart(operation => this.startOperation(monitorable, operation));
            monitorable.onEnd((operation) => this.endOperation(monitorable, operation));
        });
    }

    public startOperation(monitorable: Monitorable, name: string): void {
        this.activeOperations.get(monitorable).push(name);
        if (this.numActiveOperations === 1) {
            this.stateChanged.emit(true);
        }
    }

    public endOperation(monitorable: Monitorable, name: string): void {
        if (this.activeOperations.get(monitorable).indexOf(name) < 0) {
            return;
        }
        Arrays.remove(this.activeOperations.get(monitorable), name);
        if (this.numActiveOperations === 0) {
            this.stateChanged.emit(false);
        }
    }

    public get numActiveOperations(): number {
        let num = 0;
        this.subjects.forEach(monitorable => {
            num += this.activeOperations.get(monitorable).length;
        });
        return num;
    }

    public get hasActiveOperation(): boolean {
        return this.numActiveOperations > 0;
    }
}
