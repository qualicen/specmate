import { ConverterBase } from './converter-base';
import { ValuePair } from '../providers/properties/value-pair';

export type VariableAndCondition = { variable: string, condition: string };

export class VariableConditionToNameConverter extends ConverterBase<VariableAndCondition, ValuePair> {
    public convertTo(item: VariableAndCondition): ValuePair {
        if (item.variable === undefined || item.condition === undefined) {
            return name;
        }
        return new ValuePair(item.variable, item.condition);
    }

    public convertFrom(value: ValuePair, item: VariableAndCondition): { variable: string, condition: string } {
        return {
            variable: value.variable,
            condition: value.condition
        };
    }
}
