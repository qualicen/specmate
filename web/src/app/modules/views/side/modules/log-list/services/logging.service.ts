import { EventEmitter, Injectable } from '@angular/core';
import { Config } from '../../../../../../config/config';
import { ELogSeverity } from './e-log-severity';
import { LogElement } from './log-element';

@Injectable()
export class LoggingService {
    private logHistory: LogElement[] = [];

    public logEvent = new EventEmitter<LogElement>();

    public get logs(): LogElement[] {
        return this.logHistory;
    }

    public debug(message: string, url?: string): void {
        this.log(message, ELogSeverity.DEBUG, url);
    }

    public info(message: string, url?: string): void {
        this.log(message, ELogSeverity.INFO, url);
    }

    public warn(message: string, url?: string): void {
        this.log(message, ELogSeverity.WARN, url);
    }

    public error(message: string, url?: string): void {
        this.log(message, ELogSeverity.ERROR, url);
    }

    private log(message: string, severity: ELogSeverity, url?: string): void {
        let logElement: LogElement = new LogElement(message, severity, new Date(), url);
        this.logHistory.unshift(logElement);
        if (this.logHistory.length > Config.LOG_LENGTH) {
            this.logHistory = this.logHistory.slice(0, Config.LOG_LENGTH);
        }
        this.logEvent.next(logElement);
    }
}
