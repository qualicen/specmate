import { Component, Input } from '@angular/core';
import { IContainer } from '../../../../../../../model/IContainer';
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
        this.wrapRobotStep(testStep);
        
        let desc = testStep.description;
        this.selectedKeyword = testStep.name;
        testStep.description = desc;
        this.dataService.updateElement(testStep, true, Id.uuid);

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
        let fields = [];
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
        let idx = this.keywords.indexOf(this.testStep.name);
        if (idx < 0) {
            this.testStep.name = this.keywords[0];
            if (this.keywords[0] === undefined) {
                this.testStep.name = '';
            }
            this.dataService.updateElement(this.testStep, true, Id.uuid);
            idx = 0;
        }
        return this.keywords[idx];
    }

    public set selectedKeyword(keyword: string) {
        if (!keyword) {
            this.testStep.referencedTestParameters = [];
            this.dataService.updateElement(this.testStep, true, Id.uuid);
            return;
        }
        let newKeyword = this.keywords.indexOf(keyword);
        if (newKeyword < 0) {
            newKeyword = 0;
        }
        if (newKeyword != this._keywordIndex) {
            this._keywordIndex = newKeyword;
            this.testStep.name = this.keywords[newKeyword];
            this.testStep.description = '\n'.repeat(this.keywordService.maxParameterCount);
            this.dataService.updateElement(this.testStep, true, Id.uuid);
            this.buildFormGroup();
        }
    }

    public get parameters(): string[] {
        let key = this.keywordService.getKeyword(this._keywordIndex);
        if (key == undefined) {
            return [];
        }
        return key.parameters;
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

    private wrapRobotStep(rStep: RobotStep) {
        const max = this.keywordService.maxParameterCount;
        const para: string[] = rStep.description.split('\n');
        for (let i = 0; i < max; i++) {
            let key = 'parameter' + i;
            Object.defineProperty(rStep, key, {
                get: () => {
                    return rStep.description.split('\n')[i];
                },
                set: (value: string) => {
                    let param = rStep.description.split('\n');
                    param[i] = value;
                    rStep.description = param.join('\n');
                },
              });
              let def = para[i];
              if (def === undefined || def === null) {
                  def = '';
              }
              rStep[key] = def;
        }
    }
}
