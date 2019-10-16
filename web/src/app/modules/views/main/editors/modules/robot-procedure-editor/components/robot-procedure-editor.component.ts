import { Component } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { DragulaService } from 'ng2-dragula';
import { TestStepFactory } from '../../../../../../../factory/test-step-factory';
import { IContainer } from '../../../../../../../model/IContainer';
import { IContentElement } from '../../../../../../../model/IContentElement';
import { IPositionable } from '../../../../../../../model/IPositionable';
import { TestParameter } from '../../../../../../../model/TestParameter';
import { RobotProcedure } from '../../../../../../../model/RobotProcedure';
import { Type } from '../../../../../../../util/type';
import { Url } from '../../../../../../../util/url';
import { SpecmateDataService } from '../../../../../../data/modules/data-service/services/specmate-data.service';
import { NavigatorService } from '../../../../../../navigation/modules/navigator/services/navigator.service';
import { ConfirmationModal } from '../../../../../../notification/modules/modals/services/confirmation-modal.service';
import { DraggableSupportingViewBase } from '../../../base/draggable-supporting-view-base';
import { RobotKeywordService } from '../services/robot-keyword-service';
import { RobotStepFactory } from '../../../../../../../factory/robot-step-factory';

@Component({
    moduleId: module.id.toString(),
    selector: 'robot-procedure-editor',
    templateUrl: 'robot-procedure-editor.component.html',
    styleUrls: ['robot-procedure-editor.component.css']
})
export class RobotProcedureEditor extends DraggableSupportingViewBase {

    /** The test procedure being edited */
    public robotProcedure: RobotProcedure;

    public get relevantElements(): (IContentElement & IPositionable)[] {
        return this.contents as (IContentElement & IPositionable)[];
    }

    /** The contents of the parent test specification */
    private testSpecContents: IContainer[];

    /** getter for the input parameters of the parent test specification */
    public get inputParameters(): IContentElement[] {
        return this.allParameters.filter((param: TestParameter) => param.type === 'INPUT');
    }

    /** getter for the output parameters of the parent test specification */
    public get outputParameters(): IContentElement[] {
        return this.allParameters.filter((param: TestParameter) => param.type === 'OUTPUT');
    }

    /** getter for all test parameters */
    private get allParameters(): IContentElement[] {
        if (!this.testSpecContents) {
            return [];
        }
        return this.testSpecContents.filter((element: IContainer) => Type.is(element, TestParameter));
    }

    /** Constructor */
    constructor(dataService: SpecmateDataService,
        navigator: NavigatorService,
        route: ActivatedRoute,
        modal: ConfirmationModal,
        dragulaService: DragulaService,
        translate: TranslateService,
        private keyService: RobotKeywordService) {
            super(dataService, navigator, route, modal, dragulaService, translate);
    }

    public onElementResolved(element: IContainer): Promise<void> {
        return super.onElementResolved(element)
            .then(() => Type.is(element, RobotProcedure) ? Promise.resolve() : Promise.reject('Not a robot procedure'))
            .then(() => this.readParentTestSpec(element as RobotProcedure))
            .then(() => this.robotProcedure = element as RobotProcedure)
            .then(() => Promise.resolve());
    }

    /** Reads the parent test specification */
    private readParentTestSpec(robotProcedure: RobotProcedure): Promise<void> {
        let testSpecificationUrl: string = Url.parent(robotProcedure.url);
        return this.dataService.readContents(testSpecificationUrl)
            .then((contents: IContainer[]) => this.testSpecContents = contents)
            .then((contents: IContainer[]) => this.sanitizeContentPositions(true))
            .then(() => Promise.resolve());
    }

    /** Creates a new test case */
    private createNewTestStep() {
        let factory: RobotStepFactory = new RobotStepFactory(this.dataService);
        factory.create(this.robotProcedure, false);
    }

    /** Return true if all user inputs are valid  */
    protected get isValid(): boolean {
        return true;
    }

    private _keysLoaded = false;
    public get keysLoaded(): boolean {
        if (this.keyService.isLoaded != this._keysLoaded) {
            Promise.resolve().then(() => this._keysLoaded = this.keyService.isLoaded);
        }
        return this._keysLoaded;
    }
}