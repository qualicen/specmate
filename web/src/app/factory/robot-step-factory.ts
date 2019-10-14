import { Config } from '../config/config';
import { IContainer } from '../model/IContainer';
import { RobotStep } from '../model/RobotStep';
import { Id } from '../util/id';
import { Type } from '../util/type';
import { Url } from '../util/url';
import { ElementFactoryBase } from './element-factory-base';

export class RobotStepFactory extends ElementFactoryBase<RobotStep> {
    public create(parent: IContainer, commit: boolean, compoundId?: string, name?: string): Promise<RobotStep> {
        compoundId = compoundId || Id.uuid;
        let id = Id.uuid;
        let url: string = Url.build([parent.url, id]);
        let position: number = this.contents ? this.contents.length : 0;
        let robotStep: RobotStep = new RobotStep();
        robotStep.name = name || Config.ROBOTSTEP_NAME + ' ' + ElementFactoryBase.getDateStr();
        robotStep.description = Config.ROBOTSTEP_ACTION;
        robotStep.expectedOutcome = Config.ROBOTSTEP_EXPECTED_OUTCOME;
        robotStep.id = id;
        robotStep.url = url;
        robotStep.position = position;
        robotStep.referencedTestParameters = [];

        return this.dataService.readContents(parent.url)
            .then((contents: IContainer[]) =>
                robotStep.position = contents.filter((element: IContainer) => Type.is(element, RobotStep)).length)
            .then(() => this.dataService.createElement(robotStep, true, compoundId))
            .then(() => commit ? this.dataService.commit('create') : Promise.resolve())
            .then(() => robotStep);
    }
}
