import { ConverterBase } from './converter-base';
import { CEGmxModelNode } from '../providers/properties/ceg-mx-model-node';
import { CEGmxModelLinkedNode } from '../providers/properties/ceg-mx-model-linked-node';

export type VariableAndCondition = { variable: string, condition: string, type: string };

export class VariableConditionToNameConverter extends ConverterBase<VariableAndCondition, CEGmxModelNode | CEGmxModelLinkedNode> {
    public convertTo(item: VariableAndCondition): CEGmxModelNode | CEGmxModelLinkedNode {
        if (item.variable === undefined || item.condition === undefined) {
            return name;
        }
        if (item.condition === undefined) {
            return new CEGmxModelLinkedNode(item.variable, item.condition);
        }
        return new CEGmxModelNode(item.variable, item.condition, item.type);
    }

    public convertFrom(value: CEGmxModelNode | CEGmxModelLinkedNode, item: VariableAndCondition): { variable: string, condition: string, type: string } {
        let type = 'AND';
        if (value instanceof CEGmxModelNode) {
            type = value.type;
        }
        return {
            variable: value.variable,
            condition: value.condition,
            type: type
        };
    }
}
