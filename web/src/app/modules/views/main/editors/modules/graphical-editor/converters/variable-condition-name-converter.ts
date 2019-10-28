import { ConverterBase } from './converter-base';
import { CEGmxModelNode } from '../providers/properties/ceg-mx-model-node';

export type VariableAndCondition = { variable: string, condition: string, type: string };

export class VariableConditionToNameConverter extends ConverterBase<VariableAndCondition, CEGmxModelNode> {
    public convertTo(item: VariableAndCondition): CEGmxModelNode {
        if (item.variable === undefined || item.condition === undefined) {
            return name;
        }
        return new CEGmxModelNode(item.variable, item.condition, item.type);
    }

    public convertFrom(value: CEGmxModelNode, item: VariableAndCondition): { variable: string, condition: string, type: string } {
        return {
            variable: value.variable,
            condition: value.condition,
            type: value.type
        };
    }
}
