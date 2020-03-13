import { ElementFactoryBase } from '../../../../../../../../factory/element-factory-base';
import { ProcessDecisionFactory } from '../../../../../../../../factory/process-decision-factory';
import { IContainer } from '../../../../../../../../model/IContainer';
import { Process } from '../../../../../../../../model/Process';
import { ProcessDecision } from '../../../../../../../../model/ProcessDecision';
import { SpecmateDataService } from '../../../../../../../data/modules/data-service/services/specmate-data.service';
import { SelectedElementService } from '../../../../../../side/modules/selected-element/services/selected-element.service';
import { CreateNodeToolBase } from '../create-node-tool-base';
import { ShapeProvider } from '../../../graphical-editor/providers/properties/shape-provider';

export class DecisionTool extends CreateNodeToolBase<ProcessDecision> {

    protected modelType: { className: string; } = Process;

    public name = 'tools.addDecision';
    public icon = 'plus';
    public style = new ShapeProvider(Process).getStyle(ProcessDecision);

    constructor(parent: IContainer,
        dataService: SpecmateDataService,
        selectedElementService: SelectedElementService) {
        super(dataService, selectedElementService, parent);
    }

    protected getElementFactory(coords: { x: number; y: number; }): ElementFactoryBase<ProcessDecision> {
        return new ProcessDecisionFactory(coords, this.dataService);
    }
}
