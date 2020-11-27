import { EventEmitter } from '@angular/core';

export abstract class Monitorable {
    private startOperation: EventEmitter<string> = new EventEmitter<string>();
    private endOperation: EventEmitter<string> = new EventEmitter<string>();

    protected start(operation = this.constructor.name): void {
        this.startOperation.emit(operation);
    }

    protected end(operation = this.constructor.name): void {
        this.endOperation.emit(operation);
    }

    public onStart(callback: (op: string) => void): void {
        this.startOperation.subscribe(callback);
    }

    public onEnd(callback: (op: string) => void): void {
        this.endOperation.subscribe(callback);
    }
}
