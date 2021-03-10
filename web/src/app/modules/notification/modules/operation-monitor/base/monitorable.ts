import { EventEmitter } from '@angular/core';
import { Subscription } from 'rxjs';

export abstract class Monitorable {
    private startOperation: EventEmitter<string> = new EventEmitter<string>();
    public endOperation: EventEmitter<string> = new EventEmitter<string>();

    public start(operation = this.constructor.name): void {
        this.startOperation.emit(operation);
    }

    public end(operation = this.constructor.name): void {
        this.endOperation.emit(operation);
    }

    public onStart(callback: (op: string) => void): Subscription {
        return this.startOperation.subscribe(callback);
    }

    public onEnd(callback: (op: string) => void): Subscription {
        return this.endOperation.subscribe(callback);
    }
}
