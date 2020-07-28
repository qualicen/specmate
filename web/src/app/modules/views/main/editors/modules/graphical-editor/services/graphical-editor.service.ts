import { Injectable, EventEmitter } from '@angular/core';

@Injectable()
export class GraphicalEditorService {

    public initModel: EventEmitter<void> = new EventEmitter<void>();
    public initModelFinish: EventEmitter<void> = new EventEmitter<void>();

    constructor() {
    }

    public triggerGraphicalModelInit(): void {
        this.initModel.emit();
    }

    public triggerGraphicalModelInitFinish(): void {
        this.initModelFinish.emit();
    }
}
