import { TranslateService } from '@ngx-translate/core';
import { CEGLinkedNode } from 'src/app/model/CEGLinkedNode';
import { CEGNode } from 'src/app/model/CEGNode';
import { Type } from 'src/app/util/type';
import { Url } from 'src/app/util/url';
import { BatchOperation } from '../../../../../model/BatchOperation';
import { IContainer } from '../../../../../model/IContainer';
import { Id } from '../../../../../util/id';
import { LoggingService } from '../../../../views/side/modules/log-list/services/logging.service';
import { Command } from './command';
import { EOperation } from './e-operation';
import { SpecmateDataService } from './specmate-data.service';

export class Scheduler {

    private commands: Command[] = [];
    public elementsToReload: string[] = [];

    constructor(private dataService: SpecmateDataService, private logger: LoggingService, private translate: TranslateService) { }

    public toBatchOperation(): BatchOperation {
        const batchOperation = new BatchOperation();
        batchOperation.operations = this.unresolvedCommands.map(command => command.operation);
        return batchOperation;
    }

    public resolveBatchOperation(batchOperation: BatchOperation): void {
        batchOperation.operations.forEach(operation => {
            const command = this.commands.find(command => command.operation === operation);
            command.resolve();
        });
    }
    public getElementsToReload(): string[] {
        return this.elementsToReload;
    }

    public get unresolvedCommands(): Command[] {
        return this.commands.filter((command: Command) => !command.isResolved);
    }

    private get lastCommand(): Command {
        return this.unresolvedCommands[this.unresolvedCommands.length - 1];
    }

    private popCompoundCommands(): Command[] {
        let lastCommand: Command = this.lastCommand;
        if (!lastCommand) {
            return undefined;
        }

        let unresolvedCompoundCommands: Command[] =
            this.unresolvedCommands.filter((command: Command) => command.compoundId === lastCommand.compoundId);
        for (let i = unresolvedCompoundCommands.length - 1; i >= 0; i--) {
            let index: number = this.commands.indexOf(unresolvedCompoundCommands[i]);
            this.commands.splice(index, 1);
        }
        return unresolvedCompoundCommands;
    }

    private getInitialValue(url: string): IContainer {
        let initCommand: Command =
            this.commands.filter((command: Command) => command.operationType === EOperation.INIT && command.originalValue.url === url)[0];

        if (initCommand) {
            return initCommand.originalValue;
        }

        return undefined;
    }

    public undo(): boolean {
        let lastCommands: Command[] = this.popCompoundCommands();

        if (!lastCommands || lastCommands.length < 1) {
            this.logger.info(this.translate.instant('noCommandsLeft'));
            return false;
        }

        lastCommands.reverse().forEach((command: Command) => this.undoSingleCommand(command));
        return true;
    }


    public undoAll(): void {
        while (this.undo()) {
            this.logger.debug(this.translate.instant('undoAll'));
        }
    }


    private undoSingleCommand(command: Command): void {
        if (!command) {
            this.logger.warn(this.translate.instant('commandWasNotDefined'));
            return;
        }
        let originalValue: IContainer = command.originalValue;

        // First, we check whether this element was initialized (this happens, if it was read from the server)
        if (!originalValue) {
            originalValue = this.getInitialValue(command.url);
        }

        switch (command.operationType) {
            case EOperation.CREATE:
                this.dataService.undoCreate(command.newValue.url);
                break;
            case EOperation.UPDATE:
                this.dataService.undoUpdate(originalValue);
                break;
            case EOperation.DELETE:
                this.dataService.undoDelete(originalValue);
                if (Type.is(originalValue, CEGNode)) {
                    let n = originalValue as CEGNode;
                    for (let linkingNodeProxy of n.linksFrom) {
                        this.elementsToReload.splice(this.elementsToReload.indexOf(linkingNodeProxy.url), 1);
                    }
                }
                if (Type.is(originalValue, CEGLinkedNode)) {
                    if (((originalValue as CEGLinkedNode).linkTo)) {
                        this.elementsToReload.splice(this.elementsToReload.indexOf((originalValue as CEGLinkedNode).linkTo.url), 1);
                    }
                }
                break;
            default:
                break;
        }
    }

    public clearCommits(): void {
        this.commands = this.commands.filter((command: Command) => command.operationType === EOperation.INIT || !command.isResolved);
    }
    public clearElementsToReload(): void {
        this.elementsToReload = [];
    }

    public get hasCommits(): boolean {
        return this.countOpenCommits > 0;
    }

    public get countOpenCommits(): number {
        return this.unresolvedCommands.length;
    }

    private getCommands(url: string): Command[] {
        return this.commands.filter((command: Command) => command.url === url);
    }

    private getLastStoredValue(url: string): IContainer {
        let commands: Command[] = this.getCommands(url);
        if (commands && commands.length > 0) {
            let index = commands.length - 1;
            while (index >= 0) {
                const lastStoredValue = commands[index].newValue;
                if (lastStoredValue !== undefined) {
                    lastStoredValue.url = url;
                    return lastStoredValue;
                }
                index--;
            }
        }
        return undefined;
    }

    private getFirstUnresolvedCommand(url: string): Command {
        return this.unresolvedCommands.filter((command: Command) => command.url === url)[0];
    }

    private getLastUnresolvedCommand(url?: string): Command {
        if (!url) {
            return this.unresolvedCommands[this.unresolvedCommands.length - 1];
        }
        let commandsForUrl: Command[] = this.unresolvedCommands.filter((command: Command) => command.url === url);
        return commandsForUrl[commandsForUrl.length - 1];
    }

    public initElement(element: IContainer): void {
        if (!element) {
            return;
        }
        if (this.shouldInit(element.url)) {
            let command: Command = new Command(element.url, element, element, EOperation.INIT, Id.uuid);
            this.commands.push(command);
        }
    }

    private shouldInit(url: string): boolean {
        return !this.commands.some((command: Command) => command.operationType === EOperation.INIT && command.url === url);
    }

    public schedule(url: string, operation: EOperation, newValue: IContainer, originalValue: IContainer, compoundId: string): void {
        if (!originalValue) {
            originalValue = this.getLastStoredValue(url);
        }

        let command: Command = new Command(url, originalValue, newValue, operation, compoundId);

        switch (command.operationType) {
            case EOperation.CREATE:
                this.scheduleCreateCommand(command);
                break;
            case EOperation.UPDATE:
                this.scheduleUpdateCommand(command);
                break;
            case EOperation.DELETE:
                this.scheduleDeleteCommand(command);
                break;
        }
    }

    private unScheduleLastCommand(url: string): boolean {
        let index: number = this.commands.indexOf(this.getLastUnresolvedCommand(url));
        if (index >= 0) {
            this.commands.splice(index, 1);
            return true;
        }
        return false;
    }

    private scheduleCreateCommand(command: Command) {
        this.commands.push(command);
    }

    private scheduleUpdateCommand(command: Command): void {
        if (!command.originalValue) {
            return;
        }
        if (!this.currentlyExists(command.url)) {
            return;
        }
        let lastCommand: Command = this.getLastUnresolvedCommand();
        if (this.shouldMerge(lastCommand, command)) {
            command = lastCommand.mergeKeepOriginalValue(command);
            this.unScheduleLastCommand(command.url);
        }
        if (command.isDifference) {
            this.commands.push(command);
        }
    }

    private scheduleDeleteCommand(command: Command): void {
        if (!this.currentlyExists(command.url)) {
            return;
        }
        let lastCommand: Command = this.getLastUnresolvedCommand();
        if (this.isTrivialCreateDeletePair(lastCommand, command)) {
            this.unScheduleLastCommand(lastCommand.url);
            return;
        }
        this.commands.push(command);
        if (Type.is(command.originalValue, CEGNode)) {
            let n = command.originalValue as CEGNode;
            for (let linkingNodeProxy of n.linksFrom) {
                this.elementsToReload.push(linkingNodeProxy.url);
            }

        }
        if (Type.is(command.originalValue, CEGLinkedNode)) {
            if (((command.originalValue as CEGLinkedNode).linkTo)) {
                this.elementsToReload.push((command.originalValue as CEGLinkedNode).linkTo.url);
            }
        }
    }

    private currentlyExists(url: string): boolean {
        let commands: Command[] = this.getCommands(url);
        if (commands.length === 0) {
            this.logger.error(this.translate.instant('triedToCheckExistenceOfUnknownElement'), url);
            throw new Error(this.translate.instant('triedToCheckExistenceOfUnknownElement') + ' ' + url);
        }
        let lastCommand: Command = commands[commands.length - 1];
        return lastCommand.operationType !== EOperation.DELETE;
    }

    private shouldMerge(c1: Command, c2: Command): boolean {
        if (c1 && c2) {
            return c1.operationType === EOperation.UPDATE &&
                c2.operationType === EOperation.UPDATE &&
                c1.changedSameFields(c2) && c1.url === c2.url;
        }
        return false;
    }

    private isTrivialCreateDeletePair(command1: Command, command2: Command): boolean {
        if (command1 === undefined || command2 === undefined) {
            return false;
        }
        return command1.operationType === EOperation.CREATE
            && command2.operationType === EOperation.DELETE
            && command1.newValue.id === command2.originalValue.id;
    }

    public isVirtualElement(url: string): boolean {
        return this.getCommands(url).some((command: Command) => command.operationType === EOperation.CREATE && !command.isResolved);
    }

    public resolve(url: string): void {
        this.logger.debug(this.translate.instant('resolve'), url);
        let firstCommand: Command = this.getFirstUnresolvedCommand(url);
        if (firstCommand) {
            firstCommand.resolve();
            return;
        }
        this.logger.warn(this.translate.instant('commandNotFoundForResolve'), url);
    }

}
