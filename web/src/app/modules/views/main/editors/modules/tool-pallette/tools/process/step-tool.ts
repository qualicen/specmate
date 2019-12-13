import { ElementFactoryBase } from '../../../../../../../../factory/element-factory-base';
import { ProcessStepFactory } from '../../../../../../../../factory/process-step-factory';
import { Process } from '../../../../../../../../model/Process';
import { ProcessStep } from '../../../../../../../../model/ProcessStep';
import { CreateNodeToolBase } from '../create-node-tool-base';
import { ShapeProvider } from '../../../graphical-editor/providers/properties/shape-provider';

export class StepTool extends CreateNodeToolBase<ProcessStep> {

    protected modelType: { className: string; } = Process;

    public icon = 'plus';
    public name = 'tools.addStep';
    public style = new ShapeProvider(Process).getStyle(ProcessStep);

    protected getElementFactory(coords: { x: number; y: number; }): ElementFactoryBase<ProcessStep> {
        return new ProcessStepFactory(coords, this.dataService);
    }
}
