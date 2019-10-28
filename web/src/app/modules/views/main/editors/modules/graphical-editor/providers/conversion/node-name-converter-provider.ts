import { ProviderBase } from '../properties/provider-base';
import { ConverterBase } from '../../converters/converter-base';
import { VariableConditionToNameConverter } from '../../converters/variable-condition-name-converter';
import { ProcessNodeToNameConverter } from '../../converters/process-node-to-name-converter';
import { CEGmxModelNode } from '../properties/ceg-mx-model-node';

export class NodeNameConverterProvider extends ProviderBase {
    public get nodeNameConverter(): ConverterBase<any, string|CEGmxModelNode> {
        if (this.isCEGModel) {
            return new VariableConditionToNameConverter();
        } else if (this.isProcessModel) {
            return new ProcessNodeToNameConverter();
        }
    }
}
