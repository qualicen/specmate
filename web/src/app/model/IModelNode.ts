	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export class IModelNode  {

		___nsuri: string = "http://specmate.com/20200605/model/base";
		public url: string;
		public className: string = "IModelNode";
		public static className: string = "IModelNode";
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

		// References
		
		public tracesTo: Proxy[];
		public tracesFrom: Proxy[];
		public outgoingConnections: Proxy[];
		public incomingConnections: Proxy[];

		// Containment


	}

