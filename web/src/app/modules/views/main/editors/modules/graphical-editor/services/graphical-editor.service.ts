import { Injectable, EventEmitter } from '@angular/core';
import { Monitorable } from 'src/app/modules/notification/modules/operation-monitor/base/monitorable';

@Injectable()
export class GraphicalEditorService extends Monitorable {

    public initModel: EventEmitter<void> = new EventEmitter<void>();
    public initModelFinish: EventEmitter<void> = new EventEmitter<void>();

    public static OP_INIT = 'init';
    public static OP_VALIDATION = 'validation';

    constructor() {
        super();
    }

    public triggerGraphicalModelInit(): void {
        this.initModel.emit();
    }

    public triggerGraphicalModelInitFinish(): void {
        this.initModelFinish.emit();
    }
}
