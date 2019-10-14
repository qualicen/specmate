import { Component, Input } from '@angular/core';
import { IContainer } from '../../../../../../../model/IContainer';
import { ParameterAssignment } from '../../../../../../../model/ParameterAssignment';
import { Proxy } from '../../../../../../../model/support/proxy';
import { TestParameter } from '../../../../../../../model/TestParameter';
import { RobotStep } from '../../../../../../../model/RobotStep';
import { Id } from '../../../../../../../util/id';
import { Type } from '../../../../../../../util/type';
import { Url } from '../../../../../../../util/url';
import { SpecmateDataService } from '../../../../../../data/modules/data-service/services/specmate-data.service';
import { SimpleInputFormBase } from '../../../../../../forms/modules/generic-form/base/simple-input-form-base';
import { RobotKeywordService } from '../services/robot-keyword-service';

@Component({
    moduleId: module.id.toString(),
    selector: '[robot-step-row]',
    templateUrl: 'robot-step-row.component.html',
    styleUrls: ['robot-step-row.component.css']
})
export class RobotStepRow extends SimpleInputFormBase {
    private testSpecificationContents: IContainer[];
    private testProcedureContents: IContainer[];

    @Input()
    public set testStep(testStep: RobotStep) {
        let testStepUrl: string = testStep.url;
        let testProcedureUrl: string = Url.parent(testStepUrl);
        let testCaseUrl: string = Url.parent(testProcedureUrl);
        let testSpecificationUrl: string = Url.parent(testCaseUrl);
        this.dataService.readContents(testSpecificationUrl)
            .then((contents: IContainer[]) => this.testSpecificationContents = contents)
            .then(() => this.dataService.readContents(testProcedureUrl))
            .then((contents: IContainer[]) => this.testProcedureContents = contents)
            .then(() => this.modelElement = testStep);
    }

    protected get fields(): string[] {
        let fields = ['name', 'description'];
        for (let i = 0; i < this.parameters.length; i++) {
            fields.push('parameter' + i);
        }
        return fields;
    }

    public get testStep(): RobotStep {
        return this.modelElement as RobotStep;
    }

    public get testSteps(): RobotStep[] {
        if (!this.testProcedureContents) {
            return undefined;
        }
        return this.testProcedureContents
            .filter((element: IContainer) => Type.is(element, RobotStep))
            .map((element: IContainer) => element as RobotStep);
    }

    private _keywordIndex = 0;

    public get keywords(): string[] {
        return  this.keywordService.getKeywordNames();
    }

    public get selectedKeyword(): string {
        return this.keywords[this._keywordIndex];
    }

    public set selectedKeyword(keyword: string) {
        if (!keyword) {
            this.testStep.referencedTestParameters = [];
            this.dataService.updateElement(this.testStep, true, Id.uuid);
            return;
        }
        this._keywordIndex = this.keywords.indexOf(keyword);
        if (this._keywordIndex < 0) {
            this._keywordIndex = 0;
        }
    }

    public get parameters(): string[] {
        return this.keywordService.getKeyword(this._keywordIndex).parameters;
    }

    constructor(protected dataService: SpecmateDataService, private keywordService: RobotKeywordService) {
        super();
    }

    public delete(): void {
        let compoundId: string = Id.uuid;
        this.dataService.deleteElement(this.testStep.url, true, compoundId)
            .then(() => this.dataService.sanitizeContentPositions(this.testSteps, true, compoundId));
    }

    public getPosition(testStep: RobotStep): number {
        return parseInt(String(testStep.position), 10) + 1;
    }
}
