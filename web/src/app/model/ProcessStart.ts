	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export class ProcessStart  {

		___nsuri: string = "http://specmate.com/20200605/model/processes";
		public url: string;
		public className: string = "ProcessStart";
		public static className: string = "ProcessStart";
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

