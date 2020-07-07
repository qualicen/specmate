	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export class CEGNode  {

		___nsuri: string = "http://specmate.com/20200605/model/requirements";
		public url: string;
		public className: string = "CEGNode";
		public static className: string = "CEGNode";
		// Attributes
		public id: EString;
		public name: EString;
		public description: EString;
		public recycled: EBoolean;
		public hasRecycledChildren: EBoolean;
		public x: EDouble;
		public y: EDouble;
		public width: EDouble;
		public height: EDouble;
		public type: NodeType;
		public variable: EString;
		public condition: EString;

		// References
		
		public tracesTo: Proxy[];
		public tracesFrom: Proxy[];
		public outgoingConnections: Proxy[];
		public incomingConnections: Proxy[];

		// Containment


	}

