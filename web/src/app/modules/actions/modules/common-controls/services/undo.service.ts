import { Injectable, EventEmitter } from '@angular/core';

@Injectable()
export class UndoService {

  public undoPressed: EventEmitter<void> = new EventEmitter<void>();
  private _undoEnabled: boolean;
  public redoPressed: EventEmitter<void> = new EventEmitter<void>();
  private _redoEnabled: boolean;

  constructor() {
    this._undoEnabled = false;
    this._redoEnabled = false;
  }

  public undo(): void {
    this.undoPressed.emit();
  }

  public isUndoEnabled(): boolean {
    return this._undoEnabled;
  }

  public setUndoEnabled(newUndo: boolean) {
    this._undoEnabled = newUndo;
  }

  public redo(): void {
    this.redoPressed.emit();
  }

  public isRedoEnabled(): boolean {
    return this._redoEnabled;
  }

  public setRedoEnabled(newRedo: boolean) {
    this._redoEnabled = newRedo;
  }

}
