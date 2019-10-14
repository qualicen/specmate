import { Config } from '../config/config';
import { IContainer } from '../model/IContainer';
import { RobotProcedure } from '../model/RobotProcedure';
import { RobotStep } from '../model/RobotStep';
import { Id } from '../util/id';
import { Url } from '../util/url';
import { ElementFactoryBase } from './element-factory-base';
import { RobotStepFactory } from './robot-step-factory';

export class RobotProcedureFactory extends ElementFactoryBase<RobotProcedure> {
    public create(parent: IContainer, commit: boolean, compoundId?: string, name?: string): Promise<RobotProcedure> {
        compoundId = compoundId || Id.uuid;
        let id = Id.uuid;
        let url: string = Url.build([parent.url, id]);
        let robotProcedure: RobotProcedure = new RobotProcedure();
        robotProcedure.name = name || Config.ROBOTPROCEDURE_NAME + ' ' + ElementFactoryBase.getDateStr();
        robotProcedure.description = Config.ROBOTPROCEDURE_DESCRIPTION;
        robotProcedure.id = id;
        robotProcedure.url = url;
        robotProcedure.isRegressionTest = false;

        return this.dataService.createElement(robotProcedure, true, compoundId)
            .then(() => this.createTestCase(robotProcedure, compoundId))
            .then(() => commit ? this.dataService.commit('create') : Promise.resolve())
            .then(() => robotProcedure);
    }

    private createTestCase(testProcedure: RobotProcedure, compoundId: string): Promise<RobotStep> {
        let factory: RobotStepFactory = new RobotStepFactory(this.dataService);
        return factory.create(testProcedure, false, compoundId);
    }
}
