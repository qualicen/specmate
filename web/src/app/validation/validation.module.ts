import { NgModule } from '@angular/core';
import { ContradictoryCondidionValidator } from './ceg/contradictory-condition-validator';
import { DuplicateIOVariableValidator } from './ceg/duplicate-io-variable-validator';
import { DublicateLinkedNodeValidator } from './ceg/duplicate-linked-node';
import { DuplicateNodeValidator } from './ceg/duplicate-node-validator';
import { EmptyLinkedNodeValidator } from './ceg/empty-linked-node-validator';
import { EmptyModelValidator } from './ceg/empty-model-validator';
import { InvalidNodeVariableValidator } from './ceg/invalid-node-variable-validator';
import { NodeCycleValidator } from './ceg/node-cycle-validator';
import { SingleNodesValidator } from './ceg/single-nodes-validator';
import { DecisionMultipleOutgoingConnectionsValidator } from './process/decision-multiple-outgoing-connections-validator';
import { EndNodeNoOutgoingConnectionValidator } from './process/end-node-no-outgoing-connection-validator';
import { EndNodeValidator } from './process/end-node-validator';
import { HasStepsValidator } from './process/has-steps-validator';
import { InvalidConditionTextValidator } from './process/invalid-condition-text';
import { MissingConditionValidator } from './process/missing-condition-validator';
import { NodeNoIncomingValidator } from './process/node-no-incoming-validator';
import { NodeNoOutgoingValidator } from './process/node-no-outgoing-validator';
import { NodeSingleOutgoingConnectionValidator } from './process/node-single-outgoing-connection-validator';
import { StartNodeNoIncomingConnectionValidator } from './process/start-node-no-incoming-connection-validator';
import { StartNodeValidator } from './process/start-node-validator';

@NgModule({
    imports: [
        // MODULE IMPORTS
    ],
    declarations: [
        // COMPONENTS IN THIS MODULE
    ],
    exports: [
        // THE COMPONENTS VISIBLE TO THE OUTSIDE
    ],
    providers: [
        // SERVICES (IN THIS CASE: VALIDATORS)
        DuplicateNodeValidator,
        ContradictoryCondidionValidator,
        NodeCycleValidator,
        DuplicateIOVariableValidator,
        EmptyModelValidator,
        EmptyLinkedNodeValidator,
        SingleNodesValidator,
        EndNodeValidator,
        EndNodeNoOutgoingConnectionValidator,
        HasStepsValidator,
        NodeSingleOutgoingConnectionValidator,
        MissingConditionValidator,
        NodeNoIncomingValidator,
        NodeNoOutgoingValidator,
        StartNodeValidator,
        StartNodeNoIncomingConnectionValidator,
        DecisionMultipleOutgoingConnectionsValidator,
        InvalidNodeVariableValidator,
        InvalidConditionTextValidator,
        DublicateLinkedNodeValidator
    ],
    bootstrap: [
        // COMPONENTS THAT ARE BOOTSTRAPPED HERE
    ]
})
export class ValidationModule { }
