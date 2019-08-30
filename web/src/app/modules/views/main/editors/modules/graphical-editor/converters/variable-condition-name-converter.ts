import { ConverterBase } from './converter-base';

type VariableAndCondition = { variable: string, condition: string };

export class VariableConditionToNameConverter extends ConverterBase<VariableAndCondition, string> {

    static SEPARATOR = ':';

    public convertTo(item: VariableAndCondition): string {
        if (item.variable === undefined || item.condition === undefined) {
            return name;
        }
        return item.variable + VariableConditionToNameConverter.SEPARATOR + ' ' + item.condition;
    }

    public convertFrom(value: string, item: VariableAndCondition): { variable: string, condition: string } {
        const parts = value.split(VariableConditionToNameConverter.SEPARATOR);
        const variable = parts[0].trim();
        const condition = value.substring(parts[0].length + VariableConditionToNameConverter.SEPARATOR.length).trim();
        return {
            variable: variable,
            condition: condition
        };
    }
}
