/* tslint:disable */
	import './support/gentypes';
	import { Proxy } from './support/proxy';


	export class CEGLinkedNode  {

		___nsuri: string = "http://specmate.com/20200921/model/requirements";
		public url: string;
		public className: string = "CEGLinkedNode";
		public static className: string = "CEGLinkedNode";
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
		public linkTo: Proxy;

		// Containment


	}

